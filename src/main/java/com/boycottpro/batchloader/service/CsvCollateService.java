package com.boycottpro.batchloader.service;

import com.boycottpro.batchloader.model.CompanyRecord;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CsvCollateService {

    private static final String CSV_FILE_PATH = "/static/results.csv";

    public List<CompanyRecord> collate() {
        List<CompanyRecord> companies = new ArrayList<>();
        
        try {
            InputStream inputStream = getClass().getResourceAsStream(CSV_FILE_PATH);
            if (inputStream == null) {
                log.error("CSV file not found: {}", CSV_FILE_PATH);
                return companies;
            }

            CsvMapper mapper = new CsvMapper();
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            
            MappingIterator<CompanyRecord> iterator = mapper
                    .readerFor(CompanyRecord.class)
                    .with(schema)
                    .readValues(inputStream);
            
            while (iterator.hasNext()) {
                CompanyRecord company = iterator.next();
                companies.add(company);
            }
            
            log.info("Successfully loaded {} companies from CSV", companies.size());
            
        } catch (Exception e) {
            log.error("Error reading CSV file", e);
        }
        
        return companies;
    }
}