#!/bin/bash

echo "🚀 Starting Resume Enhancer (Frontend + NLP Only)..."

cd /home/mjl/111job/project

# Create logs directory
mkdir -p logs

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
echo "   NLP service PID: $NLP_PID"

cd ..

echo "⚛️  Setting up React frontend..."
cd frontend
if [ ! -d "node_modules" ]; then
    echo "   Installing frontend dependencies..."
    npm install
fi

echo "⚛️  Starting React frontend..."
nohup npm run dev > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!
echo "   Frontend service PID: $FRONTEND_PID"

cd ..

# Save PIDs
echo "$NLP_PID,,$FRONTEND_PID" > .pids

echo ""
echo "✅ Frontend and NLP services started!"
echo ""
echo "🌐 Available services:"
echo "   Frontend:  http://localhost:3000 (or 3001 if 3000 busy)"
echo "   NLP API:   http://localhost:8000"
echo ""
echo "❌ Database services disabled due to Docker issues"
echo "❌ Backend disabled due to Maven wrapper issues"
echo ""
echo "📝 Logs: tail -f logs/nlp.log logs/frontend.log"
echo "🛑 Stop: ./stop.sh"
echo ""
echo "⏳ Waiting for services to start..."

# Wait and check
sleep 10
echo ""
echo "🔍 Service status:"

if curl -s http://localhost:8000/health > /dev/null 2>&1; then
    echo "✅ NLP service: http://localhost:8000/health"
    curl -s http://localhost:8000/health | head -1
else
    echo "❌ NLP service failed - check logs/nlp.log"
    tail -5 logs/nlp.log 2>/dev/null || echo "No logs available"
fi

if curl -s http://localhost:3000 > /dev/null 2>&1; then
    echo "✅ Frontend service: http://localhost:3000"
elif curl -s http://localhost:3001 > /dev/null 2>&1; then
    echo "✅ Frontend service: http://localhost:3001"
else
    echo "❌ Frontend service failed - check logs/frontend.log"
    tail -5 logs/frontend.log 2>/dev/null || echo "No logs available"
fi

echo ""
echo "🎯 What you can test:"
echo "1. Open the frontend URL above"
echo "2. Test NLP API: curl http://localhost:8000/health"
echo "3. Test parse endpoint: curl -X POST http://localhost:8000/parse -H 'Content-Type: application/json' -d '{\"text\":\"John Doe, Software Engineer at Google\"}'"
