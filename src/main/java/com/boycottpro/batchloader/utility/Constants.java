package com.boycottpro.batchloader.utility;

public class Constants {
    
    public static final String COMPANY_DESCRIPTION_PROMPT = """
            Generate a structured JSON description for the company: %s
            
            Enhanced Company Information:
            %s
            
            Please provide accurate information in the following JSON schema:
            {
                "name": "Company name",
                "slug": "URL-friendly company identifier",
                "sector": "Primary business sector",
                "headquarters": "City, State/Country",
                "foundingYear": "Year founded (number)",
                "employees": "Number of employees (number)",
                "products": ["List of main products/services"],
                "ticker": "Stock ticker symbol if publicly traded",
                "website": "Official website URL",
                "controversies": [
                    {
                        "description": "Brief description of controversy",
                        "date": "YYYY-MM-DD",
                        "source": "Source URL or reference"
                    }
                ],
                "sources": ["List of reference URLs used"]
            }
            
            Ensure all information is factual and well-sourced. If information is not available, use null or empty arrays as appropriate.
            """;
            
    public static final String JSON_SCHEMA = """
            {
                "type": "object",
                "properties": {
                    "name": {"type": "string"},
                    "slug": {"type": "string"},
                    "sector": {"type": "string"},
                    "headquarters": {"type": "string"},
                    "foundingYear": {"type": "number"},
                    "employees": {"type": "number"},
                    "products": {"type": "array", "items": {"type": "string"}},
                    "ticker": {"type": "string"},
                    "website": {"type": "string"},
                    "controversies": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "properties": {
                                "description": {"type": "string"},
                                "date": {"type": "string"},
                                "source": {"type": "string"}
                            },
                            "required": ["description", "date", "source"]
                        }
                    },
                    "sources": {"type": "array", "items": {"type": "string"}}
                },
                "required": ["name", "slug", "sector", "headquarters"]
            }
            """;
}