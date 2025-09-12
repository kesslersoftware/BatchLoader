# BatchLoader

BatchLoader is a Spring Boot application that uses OpenAI's API to generate structured company descriptions and persist them locally or to AWS S3. It's part of the BoycottPro platform.

## Features

- **AI-Powered Company Descriptions**: Uses OpenAI GPT-4o-mini to generate structured JSON company profiles
- **Batch Processing**: Process multiple companies from CSV file
- **Individual Processing**: Generate descriptions for single companies
- **Local Persistence**: Save generated files locally with timestamps
- **S3 Integration**: Optional upload to AWS S3
- **Quality Validation**: JSON schema validation for generated content

## Quick Start

### Prerequisites

- Java 21
- Maven 3.6+
- OpenAI API Key
- AWS CLI configured (optional, for S3 uploads)

### Environment Setup

Create environment variable:
```bash
export OPENAI_API_KEY=your_openai_api_key_here
```

### Running the Application

```bash
./mvnw spring-boot:run
```

The application will start on port 8090.

## API Endpoints

### Individual Company Processing
```bash
POST /companies/{companyName}/describe?uploadToS3=false
```

### Batch Processing
```bash
POST /companies/batch-process?uploadToS3=false
```

### Health Check
```bash
GET /companies/health
```

## Configuration

Key configuration in `application.yaml`:

- `server.port: 8090` - Application port
- `boycottpro.exportDir` - Local export directory
- `boycottpro.bucket` - S3 bucket name
- `boycottpro.awsProfile` - AWS CLI profile

## Data Flow

1. **Input**: Company name or CSV file with company data
2. **Enhancement**: Enrich company information from CSV data (CEO, industry, location, etc.)
3. **AI Generation**: Send structured prompt to OpenAI API
4. **Validation**: Validate JSON response against predefined schema
5. **Persistence**: Save locally and optionally to S3
6. **Rate Limiting**: 1-second delay between batch requests

## File Structure

Generated files are saved as:
- **Local**: `${exportDir}/${companyName}.json`
- **S3**: `${companyName}.json`

## Development

### Build
```bash
./mvnw clean package
```

### Test
```bash
./mvnw test
```

### Run with Profile
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## CI/CD

This project includes Jenkins pipeline configurations for:
- Automated testing with JaCoCo coverage
- SonarQube code quality analysis  
- Docker-based local CI/CD setup

See `JENKINS_SETUP.md` for complete CI/CD setup instructions.

## License

Private - BoycottPro Platform