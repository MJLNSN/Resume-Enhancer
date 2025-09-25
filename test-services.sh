#!/bin/bash

echo "🧪 Testing Resume Enhancer Services..."

# Function to check if a service is responding
check_service() {
    local service_name=$1
    local url=$2
    local expected_status=${3:-200}
    
    echo -n "   Testing $service_name... "
    
    response=$(curl -s -w "%{http_code}" -o /dev/null "$url" 2>/dev/null)
    
    if [ "$response" -eq "$expected_status" ] || [ "$response" -eq "200" ] || [ "$response" -eq "404" ]; then
        echo "✅ OK (HTTP $response)"
        return 0
    else
        echo "❌ FAILED (HTTP $response)"
        return 1
    fi
}

# Check if services are running
echo "🔍 Checking service health:"

# Check NLP service
check_service "NLP Health Check" "http://localhost:8000/health"

# Check Backend (should return 401 for unauthorized)
check_service "Backend API" "http://localhost:8080/api/v1/auth/login" 405

# Check Frontend
check_service "Frontend" "http://localhost:3000"

# Check Docker services
echo ""
echo "🐳 Checking Docker services:"
if docker-compose ps | grep -q "Up"; then
    echo "   ✅ Docker services are running"
else
    echo "   ❌ Docker services are not running"
fi

echo ""
echo "📊 Service URLs:"
echo "   Frontend:  http://localhost:3000"
echo "   Backend:   http://localhost:8080/api/v1"
echo "   NLP API:   http://localhost:8000"
echo "   NLP Docs:  http://localhost:8000/docs"
echo "   MinIO:     http://localhost:9001 (minioadmin/minioadmin)"

echo ""
echo "🧪 Test complete!"
