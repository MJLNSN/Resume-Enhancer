from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import spacy
import re
import logging

app = FastAPI(title="Resume NLP Service", version="1.0.0")
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@app.get("/")
async def root():
    return {"message": "Resume NLP Service is running", "version": "1.0.0"}

@app.get("/health")
async def health():
    return {"status": "healthy", "service": "nlp"}

# Load spaCy model
try:
    nlp = spacy.load("en_core_web_sm")
except OSError:
    logger.warning("spaCy model not found. Using fallback parsing.")
    nlp = None

class ParseRequest(BaseModel):
    text: str

class Experience(BaseModel):
    company: str
    title: str
    years: str
    bullets: List[str]

class Education(BaseModel):
    institution: str
    degree: str
    field: str
    years: str

class ParsedResume(BaseModel):
    skills: List[str]
    experiences: List[Experience]
    education: List[Education]

@app.get("/health")
async def health_check():
    return {"status": "healthy", "spacy_loaded": nlp is not None}

@app.post("/parse", response_model=ParsedResume)
async def parse_resume(request: ParseRequest):
    try:
        if not request.text.strip():
            raise HTTPException(status_code=400, detail="Text cannot be empty")
        
        parsed_result = parse_resume_text(request.text)
        return parsed_result
    except Exception as e:
        logger.error(f"Error parsing resume: {str(e)}")
        raise HTTPException(status_code=500, detail="Failed to parse resume")

def parse_resume_text(text: str) -> ParsedResume:
    # Clean text
    text = clean_text(text)
    
    # Extract sections
    skills = extract_skills(text)
    experiences = extract_experiences(text)
    education = extract_education(text)
    
    return ParsedResume(
        skills=skills,
        experiences=experiences,
        education=education
    )

def clean_text(text: str) -> str:
    # Remove extra whitespace
    text = re.sub(r'\s+', ' ', text)
    # Remove special characters that might interfere
    text = re.sub(r'[^\w\s\-.,()/@]', ' ', text)
    return text.strip()

def extract_skills(text: str) -> List[str]:
    skills = []
    
    # Common skill keywords
    technical_skills = [
        "python", "java", "javascript", "typescript", "react", "angular", "vue",
        "node.js", "spring", "django", "flask", "postgresql", "mysql", "mongodb",
        "aws", "azure", "docker", "kubernetes", "git", "jenkins", "ci/cd",
        "machine learning", "ai", "data science", "sql", "nosql", "redis",
        "elasticsearch", "kafka", "rabbitmq", "microservices", "rest", "graphql"
    ]
    
    # Look for skills section
    skills_section = extract_section_content(text, ["skills", "technical skills", "technologies"])
    
    if skills_section:
        # Extract from skills section
        for skill in technical_skills:
            if skill.lower() in skills_section.lower():
                skills.append(skill.title())
    else:
        # Extract from entire text
        for skill in technical_skills:
            if skill.lower() in text.lower():
                skills.append(skill.title())
    
    # Remove duplicates and sort
    return sorted(list(set(skills)))

def extract_experiences(text: str) -> List[Experience]:
    experiences = []
    
    # Find experience section
    exp_section = extract_section_content(text, ["experience", "work experience", "employment", "professional experience"])
    
    if not exp_section:
        exp_section = text
    
    # Patterns for extracting experiences
    company_patterns = [
        r"(?:at\s+|@\s*)([A-Z][a-zA-Z\s&.,Inc]+)(?:\s*(?:\||,|\n))",
        r"([A-Z][a-zA-Z\s&.,Inc]+)(?:\s*-\s*[A-Z])"
    ]
    
    title_patterns = [
        r"([A-Z][a-zA-Z\s]+(?:Engineer|Developer|Manager|Analyst|Consultant|Specialist|Lead|Senior|Junior))",
        r"((?:Senior|Junior|Lead|Principal)\s+[A-Z][a-zA-Z\s]+)"
    ]
    
    year_patterns = [
        r"(\d{4}\s*[-–]\s*(?:\d{4}|present|current))",
        r"(\d{1,2}/\d{4}\s*[-–]\s*(?:\d{1,2}/\d{4}|present|current))"
    ]
    
    # Simple extraction - this is a basic implementation
    lines = exp_section.split('\n')
    current_exp = None
    
    for line in lines:
        line = line.strip()
        if not line:
            continue
            
        # Check for company
        for pattern in company_patterns:
            match = re.search(pattern, line, re.IGNORECASE)
            if match:
                if current_exp:
                    experiences.append(current_exp)
                current_exp = Experience(
                    company=match.group(1).strip(),
                    title="",
                    years="",
                    bullets=[]
                )
                break
        
        # Check for title
        for pattern in title_patterns:
            match = re.search(pattern, line, re.IGNORECASE)
            if match and current_exp:
                current_exp.title = match.group(1).strip()
                break
        
        # Check for years
        for pattern in year_patterns:
            match = re.search(pattern, line, re.IGNORECASE)
            if match and current_exp:
                current_exp.years = match.group(1).strip()
                break
        
        # Check for bullet points
        if line.startswith(('•', '-', '*')) and current_exp:
            current_exp.bullets.append(line[1:].strip())
    
    if current_exp:
        experiences.append(current_exp)
    
    return experiences

def extract_education(text: str) -> List[Education]:
    education = []
    
    # Find education section
    edu_section = extract_section_content(text, ["education", "academic background", "qualifications"])
    
    if not edu_section:
        edu_section = text
    
    # Common degree patterns
    degree_patterns = [
        r"(Bachelor\s+of\s+[A-Z][a-zA-Z\s]+)",
        r"(Master\s+of\s+[A-Z][a-zA-Z\s]+)",
        r"(B\.?[A-Z]\.?\s*[a-zA-Z\s]*)",
        r"(M\.?[A-Z]\.?\s*[a-zA-Z\s]*)",
        r"(PhD|Ph\.D\.?\s*[a-zA-Z\s]*)"
    ]
    
    # University patterns
    university_patterns = [
        r"([A-Z][a-zA-Z\s]+University)",
        r"([A-Z][a-zA-Z\s]+College)",
        r"([A-Z][a-zA-Z\s]+Institute)"
    ]
    
    year_patterns = [
        r"(\d{4})",
        r"(\d{4}\s*[-–]\s*\d{4})"
    ]
    
    lines = edu_section.split('\n')
    current_edu = None
    
    for line in lines:
        line = line.strip()
        if not line:
            continue
        
        # Check for degree
        for pattern in degree_patterns:
            match = re.search(pattern, line, re.IGNORECASE)
            if match:
                if current_edu:
                    education.append(current_edu)
                current_edu = Education(
                    institution="",
                    degree=match.group(1).strip(),
                    field="",
                    years=""
                )
                break
        
        # Check for university
        for pattern in university_patterns:
            match = re.search(pattern, line, re.IGNORECASE)
            if match and current_edu:
                current_edu.institution = match.group(1).strip()
                break
        
        # Check for years
        for pattern in year_patterns:
            match = re.search(pattern, line)
            if match and current_edu:
                current_edu.years = match.group(1).strip()
                break
    
    if current_edu:
        education.append(current_edu)
    
    return education

def extract_section_content(text: str, section_keywords: List[str]) -> Optional[str]:
    text_lower = text.lower()
    
    for keyword in section_keywords:
        pattern = rf"\b{keyword}\b[\s:]*\n?(.*?)(?:\n\s*\n|\n[A-Z][A-Z\s]*:|\Z)"
        match = re.search(pattern, text_lower, re.DOTALL | re.IGNORECASE)
        if match:
            return match.group(1).strip()
    
    return None

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
