#!/bin/bash
set -e

echo "🚀 Setting up Resume Enhancer Production Environment"

# Configuration
APP_USER="ec2-user"
APP_DIR="/home/ec2-user/resume-enhancer"
SERVICE_DIR="/etc/systemd/system"

# Check if running as root
if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root (use sudo)"
   exit 1
fi

# Update system packages
echo "📦 Updating system packages..."
yum update -y

# Install required packages
echo "📦 Installing required packages..."
yum install -y java-17-openjdk-devel python3 python3-pip git nginx postgresql15 redis6

# Create application directory
echo "📁 Creating application directory..."
mkdir -p "$APP_DIR"
mkdir -p "$APP_DIR/logs"
chown -R "$APP_USER:$APP_USER" "$APP_DIR"

# Install Python dependencies
echo "🐍 Setting up Python environment..."
sudo -u "$APP_USER" bash -c "
    cd $APP_DIR
    python3 -m venv nlp/venv
    source nlp/venv/bin/activate
    pip install -r nlp/requirements.txt
    python -m spacy download en_core_web_sm
"

# Copy systemd service files
echo "⚙️ Installing systemd services..."
cp deploy/systemd/resume-enhancer-backend.service "$SERVICE_DIR/"
cp deploy/systemd/resume-enhancer-nlp.service "$SERVICE_DIR/"

# Reload systemd and enable services
systemctl daemon-reload
systemctl enable resume-enhancer-backend
systemctl enable resume-enhancer-nlp

# Setup nginx configuration
echo "🌐 Configuring nginx..."
cat > /etc/nginx/conf.d/resume-enhancer.conf << 'EOF'
server {
    listen 80;
    server_name _;
    
    # Redirect HTTP to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    # SSL configuration (update with your certificates)
    ssl_certificate /etc/ssl/certs/your-cert.pem;
    ssl_certificate_key /etc/ssl/private/your-key.pem;
    
    # Security headers
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    
    # Frontend static files
    location / {
        root /var/www/html/resume-enhancer;
        index index.html;
        try_files $uri $uri/ /index.html;
        
        # Cache static assets
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }
    
    # API proxy
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Increase timeout for file uploads
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # Increase max body size for file uploads
        client_max_body_size 10M;
    }
}
EOF

# Create log rotation configuration
echo "📝 Setting up log rotation..."
cat > /etc/logrotate.d/resume-enhancer << 'EOF'
/home/ec2-user/resume-enhancer/logs/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 ec2-user ec2-user
    postrotate
        systemctl reload resume-enhancer-backend resume-enhancer-nlp
    endscript
}
EOF

# Setup PostgreSQL
echo "🗄️ Configuring PostgreSQL..."
systemctl enable postgresql
systemctl start postgresql

# Create database and user (update credentials as needed)
sudo -u postgres psql << 'EOF'
CREATE DATABASE resume_enhancer_prod;
CREATE USER resume_user WITH ENCRYPTED PASSWORD 'secure_password_change_this';
GRANT ALL PRIVILEGES ON DATABASE resume_enhancer_prod TO resume_user;
ALTER USER resume_user CREATEDB;
\q
EOF

# Setup Redis
echo "📊 Configuring Redis..."
systemctl enable redis
systemctl start redis

# Create environment file template
echo "🔧 Creating environment file template..."
sudo -u "$APP_USER" bash -c "cat > $APP_DIR/.env.template << 'EOF'
# Production Environment Configuration
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=resume_enhancer_prod
POSTGRES_USER=resume_user
POSTGRES_PASSWORD=secure_password_change_this

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
ENABLE_REDIS_CACHE=true
GPT_CACHE_HOURS=24

# Cost Control
DAILY_GPT_LIMIT=50
DAILY_ENHANCEMENT_LIMIT=100

# JWT Configuration
JWT_SECRET=generate-a-secure-32-character-secret-key
JWT_EXPIRATION_HOURS=24

# OpenAI Configuration (REQUIRED)
OPENAI_API_BASE=https://api.openai.com/v1
OPENAI_API_KEY=your-openai-api-key-here
OPENAI_MODEL=gpt-3.5-turbo
OPENAI_MAX_TOKENS=2000
OPENAI_TEMPERATURE=0.3

# S3 Configuration
MINIO_ENDPOINT=
MINIO_ACCESS_KEY=
MINIO_SECRET_KEY=
MINIO_BUCKET_NAME=resume-enhancer-prod

# Application Configuration
BACKEND_PORT=8080
NLP_SERVICE_PORT=8000
SPRING_PROFILES_ACTIVE=production
EOF"

# Set proper permissions
echo "🔒 Setting permissions..."
chown -R "$APP_USER:$APP_USER" "$APP_DIR"
chmod 750 "$APP_DIR"
chmod 640 "$APP_DIR/.env.template"

# Enable firewall rules
echo "🔥 Configuring firewall..."
if command -v firewall-cmd &> /dev/null; then
    firewall-cmd --permanent --add-service=http
    firewall-cmd --permanent --add-service=https
    firewall-cmd --permanent --add-port=8080/tcp
    firewall-cmd --reload
fi

echo "✅ Production setup complete!"
echo ""
echo "📋 Next steps:"
echo "1. Copy your .env.template to .env and configure all values"
echo "2. Upload your application JAR to $APP_DIR/resume-enhancer-backend.jar"
echo "3. Upload your NLP service files to $APP_DIR/nlp/"
echo "4. Upload your frontend build files to /var/www/html/resume-enhancer/"
echo "5. Update SSL certificates in nginx configuration"
echo "6. Start services: sudo systemctl start resume-enhancer-backend resume-enhancer-nlp nginx"
echo "7. Check logs: sudo journalctl -u resume-enhancer-backend -f"
echo ""
echo "⚠️  Important: Update default passwords and secrets before starting services!"
