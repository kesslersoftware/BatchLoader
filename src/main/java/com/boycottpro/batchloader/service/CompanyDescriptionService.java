package com.boycottpro.batchloader.service;

import com.boycottpro.batchloader.model.CompanyRecord;
import com.boycottpro.batchloader.utility.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyDescriptionService {

    private final OpenAiChatModel chatModel;
    private final ObjectMapper objectMapper;

    @Value("${boycottpro.exportDir}")
    private String exportDir;

    @Value("${boycottpro.bucket}")
    private String bucketName;

    @Value("${boycottpro.awsProfile}")
    private String awsProfile;

    @Value("${boycottpro.env}")
    private String environment;

    public String generateDescription(String companyName, boolean uploadToS3) {
        try {
            log.info("Generating description for company: {}", companyName);
            
            String slug = createSlug(companyName);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            
            // Generate AI description
            String prompt = String.format(Constants.COMPANY_DESCRIPTION_PROMPT, companyName, "");
            String aiResponse = chatModel.call(prompt);
            
            // Validate JSON
            if (!isValidJson(aiResponse)) {
                throw new RuntimeException("Invalid JSON response from AI");
            }
            
            // Save locally
            saveLocalFile(slug, aiResponse, timestamp);
            
            // Upload to S3 if requested
            if (uploadToS3) {
                uploadToS3(slug, aiResponse);
            }
            
            return aiResponse;
            
        } catch (Exception e) {
            log.error("Error generating description for company: {}", companyName, e);
            throw new RuntimeException("Failed to generate description", e);
        }
    }

    public Map<String, Object> batchProcess(boolean uploadToS3, CsvCollateService csvService) {
        List<CompanyRecord> companies = csvService.collate();
        
        Map<String, Object> results = new HashMap<>();
        results.put("total", companies.size());
        results.put("processed", 0);
        results.put("errors", 0);
        results.put("results", new HashMap<String, String>());
        results.put("errorDetails", new HashMap<String, String>());
        
        for (CompanyRecord company : companies) {
            try {
                log.info("Processing company: {}", company.getCompanyName());
                
                String enhancedInfo = buildEnhancedCompanyInfo(company);
                String slug = createSlug(company.getCompanyName());
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
                
                // Generate AI description with enhanced info
                String prompt = String.format(Constants.COMPANY_DESCRIPTION_PROMPT, company.getCompanyName(), enhancedInfo);
                String aiResponse = chatModel.call(prompt);
                
                // Validate JSON
                if (!isValidJson(aiResponse)) {
                    throw new RuntimeException("Invalid JSON response from AI");
                }
                
                // Save locally
                saveLocalFile(slug, aiResponse, timestamp);
                
                // Upload to S3 if requested
                if (uploadToS3) {
                    uploadToS3(slug, aiResponse);
                }
                
                ((Map<String, String>) results.get("results")).put(company.getCompanyName(), "SUCCESS");
                results.put("processed", (Integer) results.get("processed") + 1);
                
                // Rate limiting - 1 second delay between requests
                Thread.sleep(1000);
                
            } catch (Exception e) {
                log.error("Error processing company: {}", company.getCompanyName(), e);
                ((Map<String, String>) results.get("errorDetails")).put(company.getCompanyName(), e.getMessage());
                results.put("errors", (Integer) results.get("errors") + 1);
            }
        }
        
        return results;
    }

    private String buildEnhancedCompanyInfo(CompanyRecord company) {
        StringBuilder info = new StringBuilder();
        info.append("CEO: ").append(company.getCeo() != null ? company.getCeo() : "Unknown").append("\n");
        info.append("Industry: ").append(company.getIndustry() != null ? company.getIndustry() : "Unknown").append("\n");
        info.append("Location: ").append(company.getCity() != null ? company.getCity() : "Unknown");
        if (company.getState() != null) {
            info.append(", ").append(company.getState());
        }
        info.append("\n");
        info.append("Employees: ").append(company.getEmployees() != null ? company.getEmployees() : "Unknown").append("\n");
        info.append("Stock Symbol: ").append(company.getStockSymbol() != null ? company.getStockSymbol() : "Not available").append("\n");
        info.append("Revenue: ").append(company.getRevenue() != null ? company.getRevenue() : "Unknown").append("\n");
        info.append("Company Description: ").append(company.getDescription() != null ? company.getDescription() : "No description available");
        
        return info.toString();
    }

    private String createSlug(String companyName) {
        return companyName.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .trim();
    }

    private boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            log.error("Invalid JSON: {}", json, e);
            return false;
        }
    }

    private void saveLocalFile(String slug, String content, String timestamp) throws IOException {
        Path dirPath = Paths.get(exportDir, environment, slug);
        Files.createDirectories(dirPath);
        
        // Save timestamped file
        Path timestampedFile = dirPath.resolve(slug + "_" + timestamp + ".json");
        Files.write(timestampedFile, content.getBytes());
        
        // Save latest file
        Path latestFile = dirPath.resolve("latest.json");
        Files.write(latestFile, content.getBytes());
        
        log.info("Saved files to: {}", dirPath);
    }

    private void uploadToS3(String slug, String content) {
        try {
            S3Client s3Client = S3Client.builder()
                    .region(Region.US_EAST_1)
                    .credentialsProvider(ProfileCredentialsProvider.create(awsProfile))
                    .build();

            String key = slug + ".json";
            
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("application/json")
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromString(content));
            log.info("Uploaded {} to S3 bucket: {}", key, bucketName);
            
        } catch (Exception e) {
            log.error("Failed to upload to S3", e);
            throw new RuntimeException("S3 upload failed", e);
        }
    }
}