import pytest
from fastapi.testclient import TestClient
from main import app

client = TestClient(app)

def test_health_check():
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert "status" in data
    assert data["status"] == "healthy"

def test_parse_resume_empty_text():
    response = client.post("/parse", json={"text": ""})
    assert response.status_code == 400

def test_parse_resume_simple():
    sample_text = """
    John Doe
    Software Engineer
    
    Skills: Python, JavaScript, React, AWS
    
    Experience:
    Senior Developer at Tech Corp
    2020 - Present
    • Developed web applications
    • Led team of 3 developers
    
    Education:
    Bachelor of Computer Science
    MIT University
    2016 - 2020
    """
    
    response = client.post("/parse", json={"text": sample_text})
    assert response.status_code == 200
    data = response.json()
    
    assert "skills" in data
    assert "experiences" in data
    assert "education" in data
    assert len(data["skills"]) > 0
