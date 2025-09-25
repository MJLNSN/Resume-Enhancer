#!/bin/bash

# Resume Enhancer Health Check Script
# Usage: ./health-check.sh [--detailed]

BACKEND_URL="http://localhost:8080"
NLP_URL="http://localhost:8000"
DETAILED=false

if [[ "$1" == "--detailed" ]]; then
    DETAILED=true
fi

echo "🏥 Resume Enhancer Health Check"
echo "================================"

# Function to check HTTP endpoint
check_endpoint() {
    local url=$1
    local service_name=$2
    local timeout=${3:-10}
    
    if curl -f -s --max-time "$timeout" "$url" > /dev/null 2>&1; then
        echo "✅ $service_name: OK"
        return 0
    else
        echo "❌ $service_name: FAILED"
        return 1
    fi
}

# Function to check service status
check_service() {
    local service_name=$1
    
    if systemctl is-active --quiet "$service_name"; then
        echo "✅ $service_name service: ACTIVE"
        return 0
    else
        echo "❌ $service_name service: INACTIVE"
        return 1
    fi
}

# Function to check database connection
check_database() {
    if pg_isready -h localhost -p 5432 -U postgres > /dev/null 2>&1; then
        echo "✅ PostgreSQL: OK"
        return 0
    else
        echo "❌ PostgreSQL: FAILED"
        return 1
    fi
}

# Function to check Redis connection
check_redis() {
    if redis-cli ping > /dev/null 2>&1; then
        echo "✅ Redis: OK"
        return 0
    else
        echo "❌ Redis: FAILED (optional)"
        return 0  # Redis failure is not critical
    fi
}

# Main health checks
FAILED_CHECKS=0

# Check system services
check_service "resume-enhancer-backend" || ((FAILED_CHECKS++))
check_service "resume-enhancer-nlp" || ((FAILED_CHECKS++))

# Check HTTP endpoints
check_endpoint "$BACKEND_URL/api/v1/auth/login" "Backend API" || ((FAILED_CHECKS++))
check_endpoint "$NLP_URL/health" "NLP Service" || ((FAILED_CHECKS++))

# Check dependencies
check_database || ((FAILED_CHECKS++))
check_redis

# Detailed checks if requested
if [[ "$DETAILED" == true ]]; then
    echo ""
    echo "📊 Detailed System Information"
    echo "==============================="
    
    echo "💾 Disk Usage:"
    df -h / /home 2>/dev/null | grep -v "Filesystem"
    
    echo ""
    echo "🧠 Memory Usage:"
    free -h
    
    echo ""
    echo "🔥 CPU Load:"
    uptime
    
    echo ""
    echo "🌐 Network Connections:"
    ss -tlnp | grep -E ":(80|443|8080|8000|5432|6379)"
    
    echo ""
    echo "📋 Service Logs (last 5 lines):"
    echo "Backend:"
    journalctl -u resume-enhancer-backend --no-pager -n 5 2>/dev/null || echo "No logs available"
    echo ""
    echo "NLP Service:"
    journalctl -u resume-enhancer-nlp --no-pager -n 5 2>/dev/null || echo "No logs available"
fi

echo ""
echo "================================"
if [[ $FAILED_CHECKS -eq 0 ]]; then
    echo "🎉 All critical services are healthy!"
    exit 0
else
    echo "⚠️  $FAILED_CHECKS critical service(s) failed health check"
    echo "Check individual services and logs for more details"
    exit 1
fi