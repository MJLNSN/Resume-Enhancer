# Resume Enhancer

An AI-powered resume enhancement platform that automatically parses and improves resumes with rewriting, translation, and personalized suggestions.

## Architecture

- **Frontend**: React + TypeScript (Vite)
- **Backend API Gateway**: Java Spring Boot + Maven
- **NLP Service**: Python FastAPI + spaCy/transformers
- **Database**: PostgreSQL (jsonb for parsing/suggestions)
- **Storage**: AWS S3 / MinIO
- **Deployment**: EC2, Lambda, RDS, S3

## Quick Start (Local Development)

### Prerequisites
- Docker & Docker Compose
- Java 17+ (OpenJDK recommended)
- Python 3.11+
- Node.js 18+

### One-Click Setup
```bash
git clone <repository-url>
cd project
cp .env.example .env  # Configure your local settings (optional for dev)
./start.sh  # Starts all services automatically
```

Access the application at **http://localhost:3000**

### Manual Setup
If you prefer to start services manually:

1. **Start infrastructure**:
   ```bash
   docker-compose up -d  # PostgreSQL + MinIO
   ```

2. **Start backend services**:
   ```bash
   # Terminal 1: Java API Gateway
   cd backend && ./mvnw spring-boot:run
   
   # Terminal 2: Python NLP Service
   cd nlp && pip install -r requirements.txt
   python -m spacy download en_core_web_sm  # Optional
   uvicorn main:app --reload --port 8000
   ```

3. **Start frontend**:
   ```bash
   cd frontend && npm install && npm run dev
   ```

### Stop Services
```bash
./stop.sh  # Stops all services
```

## Development Workflow

The project follows a strict development sequence across 10 phases:
0. Environment Setup → 1. Infrastructure → 2. Upload & Text Extraction → 3. Resume Parsing → 4. Enhancement → 5. Suggestions → 6. Translation → 7. Export & Versions → 8. Frontend Polish → 9. CI/CD → 10. Security & Optimization

## Core Features

- **Resume Upload & Analysis**: PDF/TXT upload with intelligent text extraction and NLP parsing
- **AI Enhancement**: GPT-powered resume rewriting with job-specific optimizations
- **Smart Suggestions**: AI-generated improvement recommendations (3-5 actionable items)
- **Multi-language Support**: Seamless Chinese⇄English translation preserving technical terms
- **Export & Comparison**: Markdown/PDF export with side-by-side version comparison
- **Version Management**: Track all enhanced versions with timestamp and metadata
- **Secure Storage**: S3-compatible file storage with JWT authentication
