#!/bin/bash

echo "🚀 Starting Resume Enhancer (Simplified Mode)..."

cd /home/mjl/111job/project

# Check if .env exists
if [ ! -f ".env" ]; then
    echo "⚠️  .env file not found, creating from template..."
    cp .env.template .env
fi

# Create logs directory
mkdir -p logs

echo "📦 Starting infrastructure services (PostgreSQL + MinIO + Redis)..."
# Try local docker images first
docker compose up -d --pull never 2>/dev/null || {
    echo "   Pulling images and starting..."
    docker compose up -d
}

# Wait for services to be ready
echo "⏳ Waiting for infrastructure services to start..."
sleep 15

echo "🔧 Setting up Python NLP service..."
cd nlp
if [ ! -d "venv" ]; then
    echo "   Creating Python virtual environment..."
    python3 -m venv venv
    source venv/bin/activate
    echo "   Installing Python dependencies..."
    pip install -r requirements.txt
    # Try to download spaCy model
    python -m spacy download en_core_web_sm 2>/dev/null || echo "   ⚠️  spaCy model not downloaded, will use fallback"
else
    source venv/bin/activate
fi

echo "🐍 Starting Python NLP service..."
nohup uvicorn main:app --host 0.0.0.0 --port 8000 > ../logs/nlp.log 2>&1 &
NLP_PID=$!

cd ..

echo "☕ Setting up React frontend..."
cd frontend
if [ ! -d "node_modules" ]; then
    echo "   Installing frontend dependencies..."
    npm install
fi

echo "⚛️  Starting React frontend..."
nohup npm run dev > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!

cd ..

# For now, skip Java backend due to Maven wrapper issues
echo "⚠️  Java backend temporarily disabled due to Maven wrapper issues"
echo "   You can manually start it later with: cd backend && mvn spring-boot:run"

# Save PIDs for cleanup
echo "$NLP_PID,,$FRONTEND_PID" > .pids

echo ""
echo "✅ Services started (simplified mode)!"
echo ""
echo "🌐 Available services:"
echo "   Frontend:  http://localhost:3000"
echo "   NLP API:   http://localhost:8000"
echo "   MinIO:     http://localhost:9001 (minioadmin/minioadmin)"
echo "   Redis:     localhost:6379"
echo ""
echo "❌ Backend (Java) not started - Maven wrapper issue"
echo "   To start manually:"
echo "   1. Install Maven: sudo apt install maven"
echo "   2. cd backend && mvn spring-boot:run"
echo ""
echo "📝 Logs: tail -f logs/nlp.log logs/frontend.log"
echo "🛑 Stop: ./stop.sh"

# Wait and check services
echo ""
echo "🔍 Checking service status..."
sleep 10

if curl -s http://localhost:8000/health > /dev/null 2>&1; then
    echo "✅ NLP service is running"
else
    echo "❌ NLP service failed to start"
fi

if curl -s http://localhost:3000 > /dev/null 2>&1; then
    echo "✅ Frontend service is running"
elif curl -s http://localhost:3001 > /dev/null 2>&1; then
    echo "✅ Frontend service is running on port 3001"
else
    echo "❌ Frontend service failed to start"
fi

# Check Docker services
echo ""
echo "🐳 Docker services:"
docker compose ps 2>/dev/null

echo ""
echo "🎯 Next steps:"
echo "1. Open http://localhost:3000 for the frontend"
echo "2. Install Maven to enable backend: sudo apt install maven"
echo "3. Test NLP service: curl http://localhost:8000/health"
