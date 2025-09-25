# Resume Enhancer

AI-powered resume enhancement for your next career move.

## Features

- **AI-Powered Enhancement**: GPT-powered resume formatting with job-specific optimization
- **Multi-language Support**: Format and translate resumes in 7+ languages (English, Chinese, Japanese, Spanish, French, German, Korean)
- **Smart Suggestions**: Personalized career advice based on job descriptions
- **Multiple Input Formats**: Support for PDF, text, and Markdown files
- **Export Options**: Download enhanced resumes in Markdown or HTML format
- **Usage Tracking**: Daily limits with reset notifications

## Tech Stack

- **Frontend**: React + TypeScript + Vite + Tailwind CSS
- **Backend**: Java Spring Boot + Maven
- **NLP Service**: Python FastAPI + spaCy
- **Database**: PostgreSQL with JSONB support
- **Authentication**: JWT tokens
- **Storage**: Local file system

## Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- Python 3.11+
- PostgreSQL

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/MJLNSN/Resume-Enhancer.git
   cd Resume-Enhancer
   ```

2. **One-click startup**
   ```bash
   ./scripts/start.sh  # Starts all services
   ```

3. **Stop all services**
   ```bash
   ./scripts/stop.sh   # Stops all services
   ```

4. **Development mode** (frontend only)
   ```bash
   ./scripts/dev.sh    # Only starts frontend
   ```

### Manual Setup (if needed)

1. **Start infrastructure**
   ```bash
   docker-compose up -d  # PostgreSQL + Redis
   ```

2. **Backend setup**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

3. **NLP service setup**
   ```bash
   cd nlp
   pip install -r requirements.txt
   python main.py
   ```

4. **Frontend setup**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

### Access

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **NLP Service**: http://localhost:8000

## Usage

1. **Register/Login** to create an account
2. **Upload Resume** via PDF, text input, or Markdown file
3. **Choose Output Language** from the dropdown (default: English)
4. **Add Job Description** (optional) for targeted optimization
5. **Enhance Resume** to get formatted, optimized output
6. **Get Suggestions** for career improvement recommendations
7. **Download** results in Markdown or HTML format

## API Configuration

Update `backend/src/main/resources/application.yml`:

```yaml
app:
  openai:
    api-base: https://api.chatanywhere.org/v1
    api-key: your-gpt-api-key
  usage:
    daily-gpt-limit: 10
    daily-enhancement-limit: 20
```

## Development

### Running Tests

```bash
# Backend tests
cd backend && mvn test

# Frontend build
cd frontend && npm run build
```

### CI/CD

The project includes GitHub Actions workflows for:
- Automated testing (Java, Python, TypeScript)
- Build verification
- Code quality checks

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests
5. Submit a pull request

## License

This project is open source and available under the MIT License.