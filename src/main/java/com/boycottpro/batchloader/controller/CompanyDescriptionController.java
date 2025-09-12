package com.boycottpro.batchloader.controller;

import com.boycottpro.batchloader.service.CompanyDescriptionService;
import com.boycottpro.batchloader.service.CsvCollateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
@Slf4j
public class CompanyDescriptionController {

    private final CompanyDescriptionService companyDescriptionService;
    private final CsvCollateService csvCollateService;

    @PostMapping("/{companyName}/describe")
    public ResponseEntity<String> describeCompany(
            @PathVariable String companyName,
            @RequestParam(defaultValue = "false") boolean uploadToS3) {
        
        try {
            log.info("Received request to describe company: {}, uploadToS3: {}", companyName, uploadToS3);
            
            String description = companyDescriptionService.generateDescription(companyName, uploadToS3);
            
            return ResponseEntity.ok(description);
            
        } catch (Exception e) {
            log.error("Error describing company: {}", companyName, e);
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Failed to generate company description: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/batch-process")
    public ResponseEntity<Map<String, Object>> batchProcessCompanies(
            @RequestParam(defaultValue = "false") boolean uploadToS3) {
        
        try {
            log.info("Starting batch processing of companies, uploadToS3: {}", uploadToS3);
            
            Map<String, Object> results = companyDescriptionService.batchProcess(uploadToS3, csvCollateService);
            
            log.info("Batch processing completed. Results: {}", results);
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("Error in batch processing", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Batch processing failed: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\": \"UP\", \"service\": \"CompanyDescriptionService\"}");
    }
}