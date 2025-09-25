#!/bin/bash

# Resume Enhancer - Stop Script
# Stop all services

set -e

echo "🛑 Stopping Resume Enhancer..."

# Get project root directory
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# Create logs directory (if not exists)
mkdir -p logs

# Stop service function
stop_service() {
    local pid_file=$1
    local service_name=$2
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            echo "🔄 Stopping $service_name (PID: $pid)..."
            kill $pid
            sleep 2
            if ps -p $pid > /dev/null 2>&1; then
                echo "⚠️  Force stopping $service_name..."
                kill -9 $pid
            fi
            echo "✅ $service_name stopped"
        else
            echo "ℹ️  $service_name was not running"
        fi
        rm -f "$pid_file"
    else
        echo "ℹ️  No PID file found for $service_name"
    fi
}

# 1. Stop frontend service
stop_service "logs/frontend.pid" "Frontend"

# 2. Stop backend service
stop_service "logs/backend.pid" "Backend"

# 3. Stop NLP service
stop_service "logs/nlp.pid" "NLP Service"

# 4. Stop possible leftover processes
echo "🔍 Checking for remaining processes..."

# Find and stop possible leftover processes
pkill -f "spring-boot:run" 2>/dev/null && echo "✅ Stopped remaining Spring Boot processes" || true
pkill -f "python.*main.py" 2>/dev/null && echo "✅ Stopped remaining Python processes" || true
pkill -f "vite.*dev" 2>/dev/null && echo "✅ Stopped remaining Vite processes" || true

# 5. Stop Docker services
echo "🐳 Stopping Docker services..."
if command -v docker-compose >/dev/null 2>&1; then
    docker-compose down
    echo "✅ Docker services stopped"
else
    echo "⚠️  Docker Compose not found, skipping..."
fi

# 6. Clean up
echo "🧹 Cleaning up..."
rm -f logs/*.pid

echo ""
echo "✅ Resume Enhancer has been stopped successfully!"
echo ""
echo "📋 To start again, run: ./scripts/start.sh"
echo ""
