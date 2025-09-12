package com.boycottpro.batchloader.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CompanyRecord {
    
    @JsonProperty("company_id")
    private String companyId;
    
    @JsonProperty("boycott_count")
    private Integer boycottCount;
    
    @JsonProperty("ceo")
    private String ceo;
    
    @JsonProperty("city")
    private String city;
    
    @JsonProperty("company_name")
    private String companyName;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("employees")
    private String employees;
    
    @JsonProperty("industry")
    private String industry;
    
    @JsonProperty("profits")
    private String profits;
    
    @JsonProperty("revenue")
    private String revenue;
    
    @JsonProperty("state")
    private String state;
    
    @JsonProperty("stock_symbol")
    private String stockSymbol;
    
    @JsonProperty("valuation")
    private String valuation;
    
    @JsonProperty("zip")
    private String zip;
}