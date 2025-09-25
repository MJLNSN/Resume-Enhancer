#!/bin/bash

# Resume Enhancer - Development Mode
# Only start the frontend, assuming the backend and NLP service are already running

set -e

echo "🚀 Starting Resume Enhancer (Development Mode)..."

# Get project root directory
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# Check necessary commands
command -v npm >/dev/null 2>&1 || { echo "❌ Node.js/npm not found. Please install Node.js 18+"; exit 1; }

echo "📂 Project root: $PROJECT_ROOT"

# Start frontend service
echo "⚛️  Starting frontend in development mode..."
cd frontend

if [ ! -d "node_modules" ]; then
    echo "📦 Installing frontend dependencies..."
    npm install
fi

echo "✅ Frontend starting on http://localhost:3000"
echo "📋 Make sure backend (8080) and NLP (8000) services are running"
echo ""

# Run frontend directly (not in background)
npm run dev
