#!/bin/bash

echo "🚀 Starting Resume Enhancer Application..."

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first:"
    echo "   sudo apt update && sudo apt install -y docker.io"
    exit 1
fi

# Check if user can access Docker
if ! docker ps &> /dev/null; then
    echo "❌ Cannot access Docker. Please run one of:"
    echo "   sudo usermod -aG docker $USER && newgrp docker"
    echo "   Or run this script with sudo"
    exit 1
fi

echo "📦 Starting infrastructure services (PostgreSQL + MinIO + Redis)..."
docker compose up -d

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 10

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17+:"
    echo "   sudo apt update && sudo apt install -y openjdk-17-jdk"
    exit 1
fi

# Check if Python is installed
if ! command -v python3 &> /dev/null; then
    echo "❌ Python3 is not installed. Please install Python 3.11+:"
    echo "   sudo apt update && sudo apt install -y python3 python3-pip python3-venv"
    exit 1
fi

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 18+:"
    echo "   curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -"
    echo "   sudo apt-get install -y nodejs"
    exit 1
fi

# Create logs directory first
mkdir -p logs

echo "🔧 Setting up Python NLP service..."
cd nlp
if [ ! -d "venv" ]; then
    python3 -m venv venv
    source venv/bin/activate
    pip install -r requirements.txt
    # Try to download spaCy model (optional)
    python -m spacy download en_core_web_sm 2>/dev/null || echo "⚠️  spaCy model not downloaded, will use fallback parsing"
else
    source venv/bin/activate
fi

echo "🐍 Starting Python NLP service..."
nohup uvicorn main:app --host 0.0.0.0 --port 8000 > ../logs/nlp.log 2>&1 &
NLP_PID=$!
echo "   NLP service PID: $NLP_PID"

cd ..

echo "☕ Setting up Java backend..."
cd backend
echo "🔨 Building Java application..."
mvn clean compile -q

echo "☕ Starting Java backend..."
nohup mvn spring-boot:run > ../logs/backend.log 2>&1 &
BACKEND_PID=$!
echo "   Backend service PID: $BACKEND_PID"

cd ..

echo "⚛️  Setting up React frontend..."
cd frontend
if [ ! -d "node_modules" ]; then
    echo "📦 Installing frontend dependencies..."
    npm install
fi

echo "⚛️  Starting React frontend..."
nohup npm run dev > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!
echo "   Frontend service PID: $FRONTEND_PID"

cd ..

# Create PID file for easy cleanup
echo "$NLP_PID,$BACKEND_PID,$FRONTEND_PID" > .pids

echo ""
echo "✅ Resume Enhancer is starting up!"
echo ""
echo "🌐 Services will be available at:"
echo "   Frontend:  http://localhost:3000 (or http://localhost:3001 if 3000 is busy)"
echo "   Backend:   http://localhost:8080/api/v1"
echo "   NLP API:   http://localhost:8000"
echo "   MinIO:     http://localhost:9001 (minioadmin/minioadmin)"
echo "   Redis:     localhost:6379"
echo ""
echo "📝 Logs are available in the 'logs/' directory"
echo "   tail -f logs/backend.log   # Watch backend logs"
echo "   tail -f logs/frontend.log  # Watch frontend logs"
echo "   tail -f logs/nlp.log       # Watch NLP logs"
echo ""
echo "🛑 To stop all services, run: ./stop.sh"
echo ""
echo "⏳ Please wait 30-60 seconds for all services to fully start..."

# Wait a bit and check services
sleep 10
echo ""
echo "🔍 Checking service status..."

if curl -s http://localhost:8000/health > /dev/null 2>&1; then
    echo "✅ NLP service is running at http://localhost:8000"
else
    echo "⚠️  NLP service may still be starting... Check logs/nlp.log"
fi

# Wait a bit more for backend
sleep 5
if curl -s -f http://localhost:8080/api/v1/auth/login > /dev/null 2>&1; then
    echo "✅ Backend service is running at http://localhost:8080"
else
    echo "⚠️  Backend service may still be starting... Check logs/backend.log"
fi

# Check what port frontend is actually using
if curl -s http://localhost:3000 > /dev/null 2>&1; then
    echo "✅ Frontend service is running at http://localhost:3000"
elif curl -s http://localhost:3001 > /dev/null 2>&1; then
    echo "✅ Frontend service is running at http://localhost:3001"
else
    echo "⚠️  Frontend service may still be starting... Check logs/frontend.log"
fi

# Check Docker services
echo ""
echo "🐳 Docker services status:"
docker compose ps

echo ""
echo "🎉 Setup complete!"
echo "💡 Pro tip: Configure your OpenAI API key in .env file for GPT features:"
echo "   echo 'OPENAI_API_KEY=sk-your-key-here' >> .env"