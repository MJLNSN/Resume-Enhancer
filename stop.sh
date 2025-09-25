#!/bin/bash

echo "🛑 Stopping Resume Enhancer Application..."

# Function to force kill processes on specific ports
kill_port() {
    local port=$1
    local pids=$(lsof -ti:$port 2>/dev/null)
    if [ ! -z "$pids" ]; then
        echo "   Killing processes on port $port: $pids"
        echo $pids | xargs kill -9 2>/dev/null || true
    fi
}

# Stop using PID file if it exists
if [ -f ".pids" ]; then
    PIDS=$(cat .pids)
    IFS=',' read -r NLP_PID BACKEND_PID FRONTEND_PID <<< "$PIDS"
    
    if [ ! -z "$NLP_PID" ] && [ "$NLP_PID" != "" ]; then
        echo "🐍 Stopping NLP service (PID: $NLP_PID)..."
        kill $NLP_PID 2>/dev/null || echo "   Process already stopped"
    fi
    
    if [ ! -z "$BACKEND_PID" ] && [ "$BACKEND_PID" != "" ]; then
        echo "☕ Stopping Backend service (PID: $BACKEND_PID)..."
        kill $BACKEND_PID 2>/dev/null || echo "   Process already stopped"
    fi
    
    if [ ! -z "$FRONTEND_PID" ] && [ "$FRONTEND_PID" != "" ]; then
        echo "⚛️  Stopping Frontend service (PID: $FRONTEND_PID)..."
        kill $FRONTEND_PID 2>/dev/null || echo "   Process already stopped"
    fi
    
    rm .pids 2>/dev/null || true
fi

# Kill processes by name and port (more aggressive cleanup)
echo "🔍 Cleaning up remaining processes..."

echo "📱 Stopping Frontend processes..."
pkill -f "vite.*dev" 2>/dev/null || true
pkill -f "npm.*dev" 2>/dev/null || true
pkill -f "node.*vite" 2>/dev/null || true
kill_port 3000
kill_port 3001

echo "☕ Stopping Backend processes..."
pkill -f "mvnw.*spring-boot:run" 2>/dev/null || true
pkill -f "spring-boot:run" 2>/dev/null || true
pkill -f "java.*resume" 2>/dev/null || true
kill_port 8080

echo "🐍 Stopping NLP processes..."
pkill -f "uvicorn.*main:app" 2>/dev/null || true
pkill -f "uvicorn" 2>/dev/null || true
pkill -f "python.*main.py" 2>/dev/null || true
kill_port 8000

# Stop Docker services
echo "🐳 Stopping Docker services..."
docker compose down 2>/dev/null || true

# Give a moment for processes to cleanly exit
sleep 2

# Final check
echo ""
echo "🔍 Final port check..."
for port in 3000 3001 8000 8080; do
    if lsof -i:$port &>/dev/null; then
        echo "⚠️  Port $port still in use"
        kill_port $port
    else
        echo "✅ Port $port is free"
    fi
done

echo ""
echo "✅ All services stopped!"
echo "📝 Logs are preserved in the 'logs/' directory"
echo ""
echo "🔧 If you still have issues, you can run:"
echo "   sudo netstat -tulpn | grep -E ':(3000|3001|8000|8080)'"
echo "   to check what's still running on these ports"