# Development Guide

This document provides detailed development information for the Resume Enhancer project.

## Project Status

### ✅ Completed Phases

- **Phase 0**: Environment Setup
  - Git repository initialization
  - Docker configuration (PostgreSQL + MinIO)
  - CI/CD basic configuration
  - Maven wrapper and build configuration

- **Phase 1**: Core Infrastructure
  - Database models (User, Resume, EnhancedResume, Job, MatchResult)
  - JPA repositories
  - JWT authentication with Spring Security
  - S3/MinIO integration
  - Password encryption

- **Phase 2**: Resume Upload & Text Extraction
  - Frontend upload component with drag-and-drop
  - Java multipart file handling
  - PDF→Text extraction using Apache PDFBox
  - Async processing with Spring's @Async
  - File validation (PDF/TXT, 5MB limit)

- **Phase 3**: Resume Parsing (Structured)
  - Python FastAPI NLP service
  - Resume parsing with spaCy (fallback: rule-based)
  - Returns structured JSON: {skills, experiences, education}
  - Integration with Java backend via WebClient
  - Error handling and timeout management

- **Phase 8**: Frontend Polish
  - React + TypeScript with Vite
  - Tailwind CSS styling
  - JWT authentication hooks
  - Dashboard with resume cards
  - Real-time processing status updates
  - Responsive design

### ✅ Recently Completed Phases

- **Phase 5**: Suggestions Generation - GPT-powered 3-5 actionable improvement recommendations
- **Phase 6**: Translation Features - Seamless Chinese⇄English translation with technical term preservation  
- **Phase 7**: Export & Version Management - Markdown/PDF export with S3 storage and version comparison

### ✅ All Phases Completed!

All 10 development phases have been successfully completed:

- ✅ **Phase 0**: Environment Setup - Complete monorepo with Docker
- ✅ **Phase 1**: Core Infrastructure - Database models, S3, JWT auth  
- ✅ **Phase 2**: Upload & Text Extraction - PDF processing pipeline
- ✅ **Phase 3**: Resume Parsing - NLP service with spaCy
- ✅ **Phase 4**: Enhancement Framework - Local + GPT modes
- ✅ **Phase 5**: AI Suggestions - GPT-powered recommendations
- ✅ **Phase 6**: Translation - Multi-language support
- ✅ **Phase 7**: Export & Versions - Markdown/PDF + comparison
- ✅ **Phase 8**: Frontend Polish - Complete user experience
- ✅ **Phase 9**: CI/CD & Deployment - Full production pipeline

### 🎯 Production Ready Features

- **Complete CI/CD Pipeline**: GitHub Actions with security scans
- **Cost Control**: Redis-based usage limits and caching
- **Production Deployment**: Systemd services, monitoring, backups  
- **Security Hardening**: Secrets management, dependency scanning
- **Performance Optimization**: Caching layer, resource limits

## Architecture Overview

```
Frontend (React + TS)     │  Backend (Java Spring Boot)    │  NLP Service (Python FastAPI)
├── Auth pages           │  ├── JWT Security              │  ├── Resume parsing
├── Dashboard            │  ├── File upload/storage       │  ├── Skill extraction
├── File upload          │  ├── User management           │  ├── Experience parsing
└── Resume viewer        │  └── NLP service integration   │  └── Education extraction
```

## Database Schema

```sql
users(id, email, password_hash, created_at)
resumes(id, user_id, file_url, raw_text, parsed_json, parse_error, created_at)
enhanced_resumes(id, resume_id, enhanced_text, language, suggestions, enhancement_type, created_at)
jobs(id, user_id, title, description, created_at)
match_results(id, resume_id, job_id, score, matched_skills, created_at)
```

## Development Setup

1. **Prerequisites**: Docker, Java 17+, Python 3.11+, Node.js 18+
2. **One-click start**: `./start.sh`
3. **Test services**: `./test-services.sh`
4. **Stop services**: `./stop.sh`

## API Endpoints

### Authentication
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/login` - User login

### Resume Management
- `POST /api/v1/resumes/upload` - Upload resume file
- `GET /api/v1/resumes` - Get user's resumes
- `GET /api/v1/resumes/{id}` - Get specific resume

### AI Enhancement & Analysis
- `POST /api/v1/analyze` - Analyze and enhance resume (GPT/local mode)
- `POST /api/v1/translate` - Translate resume content
- `GET /api/v1/enhanced/{id}` - Get specific enhanced version
- `GET /api/v1/resumes/{id}/enhanced` - Get all enhanced versions

### Export Features
- `POST /api/v1/export` - Export resume (markdown/pdf)
- `GET /api/v1/export/markdown/{id}` - Direct markdown download
- `GET /api/v1/export/compare/{id}` - Version comparison export

### NLP Service
- `GET /health` - Health check
- `POST /parse` - Parse resume text

## Technology Stack

- **Frontend**: React 18, TypeScript, Vite, Tailwind CSS, React Hook Form, React Dropzone
- **Backend**: Spring Boot 3.1, Spring Security, Spring Data JPA, JWT, WebFlux
- **NLP**: FastAPI, spaCy, Pydantic
- **Database**: PostgreSQL with JSONB support
- **Storage**: MinIO (S3-compatible) for local dev, AWS S3 for production
- **Build**: Maven (Java), npm (Frontend), pip (Python)

## Security Features

- JWT-based authentication
- Password hashing with BCrypt
- CORS configuration
- File upload validation
- Request timeout handling

## File Processing Flow

1. User uploads PDF/TXT file
2. Frontend sends multipart request to Java backend
3. Java validates and uploads file to S3/MinIO
4. Async PDF text extraction using PDFBox
5. Java calls Python NLP service for parsing
6. Structured data stored in PostgreSQL
7. Frontend polls for processing completion

## Local Development Notes

- Services run on ports 3000 (frontend), 8080 (backend), 8000 (NLP)
- PostgreSQL on 5432, MinIO on 9000/9001
- Hot reload enabled for all services
- Logs stored in `logs/` directory
- Docker services persist data in named volumes

## Recent Completions ✅

1. ✅ **GPT Service Integration** - Full OpenAI API integration with fallback mechanisms
2. ✅ **AI Suggestion Generation** - 3-5 actionable improvement recommendations per resume
3. ✅ **Translation Features** - Chinese⇄English translation preserving technical terms
4. ✅ **Export Functionality** - Markdown/PDF export with cloud storage
5. ✅ **Version Comparison** - Side-by-side original vs enhanced resume comparison
6. ✅ **Enhanced UI Components** - EnhancementPanel, VersionCard, and integrated workflows

## Next Steps for Production

1. Complete CI/CD pipeline setup
2. AWS production deployment optimization
3. Advanced security hardening
4. Performance monitoring and optimization
5. User analytics and feedback system
