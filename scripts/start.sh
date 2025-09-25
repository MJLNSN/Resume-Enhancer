#!/bin/bash

# Resume Enhancer - Start Script
# Start all necessary services

set -e

echo "🚀 Starting Resume Enhancer..."

# Check necessary commands
command -v java >/dev/null 2>&1 || { echo "❌ Java not found. Please install Java 17+"; exit 1; }
command -v python3 >/dev/null 2>&1 || { echo "❌ Python not found. Please install Python 3.11+"; exit 1; }
command -v npm >/dev/null 2>&1 || { echo "❌ Node.js/npm not found. Please install Node.js 18+"; exit 1; }
command -v docker-compose >/dev/null 2>&1 || { echo "❌ Docker Compose not found. Please install Docker Compose"; exit 1; }

# Get project root directory
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo "📂 Project root: $PROJECT_ROOT"

# 1. Start infrastructure (PostgreSQL + Redis)
echo "🐘 Starting infrastructure (PostgreSQL + Redis)..."
docker-compose up -d
sleep 5

# 2. Start NLP service
echo "🐍 Starting NLP service..."
cd nlp
if [ ! -d "venv" ]; then
    echo "📦 Creating Python virtual environment..."
    python3 -m venv venv
fi

source venv/bin/activate
pip install -r requirements.txt > /dev/null 2>&1

# Start NLP service (background)
nohup python main.py > ../logs/nlp.log 2>&1 &
NLP_PID=$!
echo "✅ NLP service started (PID: $NLP_PID)"
echo $NLP_PID > ../logs/nlp.pid

cd "$PROJECT_ROOT"

# 3. Start backend service
echo "☕ Starting backend service..."
cd backend
mkdir -p ../logs
nohup mvn spring-boot:run > ../logs/backend.log 2>&1 &
BACKEND_PID=$!
echo "✅ Backend service started (PID: $BACKEND_PID)"
echo $BACKEND_PID > ../logs/backend.pid

cd "$PROJECT_ROOT"

# 4. Start frontend service
echo "⚛️  Starting frontend service..."
cd frontend
if [ ! -d "node_modules" ]; then
    echo "📦 Installing frontend dependencies..."
    npm install
fi

nohup npm run dev > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!
echo "✅ Frontend service started (PID: $FRONTEND_PID)"
echo $FRONTEND_PID > ../logs/frontend.pid

cd "$PROJECT_ROOT"

# 5. Wait for services to start
echo "⏳ Waiting for services to start..."
sleep 10

# 6. Health check
echo "🔍 Checking service health..."
check_service() {
    local url=$1
    local name=$2
    if curl -f -s "$url" > /dev/null 2>&1; then
        echo "✅ $name is healthy"
        return 0
    else
        echo "❌ $name is not responding"
        return 1
    fi
}

# Check each service
check_service "http://localhost:8000/health" "NLP Service (8000)"
check_service "http://localhost:8080/api/v1/health" "Backend API (8080)"
check_service "http://localhost:3000" "Frontend (3000)"

echo ""
echo "🎉 Resume Enhancer is running!"
echo ""
echo "📱 Frontend:  http://localhost:3000"
echo "🔧 Backend:   http://localhost:8080"
echo "🐍 NLP:       http://localhost:8000"
echo ""
echo "📋 To stop all services, run: ./scripts/stop.sh"
echo "📋 To view logs: tail -f logs/*.log"
echo ""
