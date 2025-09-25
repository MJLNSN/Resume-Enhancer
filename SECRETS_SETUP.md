# GitHub Secrets Configuration Guide

This document explains how to configure GitHub Secrets for the Resume Enhancer CI/CD pipeline.

## Required GitHub Secrets

Navigate to your repository → Settings → Secrets and variables → Actions

### 🔑 AWS & Infrastructure Secrets

```bash
# AWS Access Credentials
AWS_ACCESS_KEY_ID: AKIA...
AWS_SECRET_ACCESS_KEY: your-secret-access-key

# AWS Infrastructure IDs
S3_FRONTEND_BUCKET: resume-enhancer-frontend-prod
CLOUDFRONT_DISTRIBUTION_ID: E1234567890123
EC2_HOST: your-ec2-instance-ip-or-hostname
```

### 🗝️ Application Secrets

```bash
# JWT Security
JWT_SECRET: your-super-secure-jwt-key-at-least-32-characters-long

# OpenAI API
OPENAI_API_BASE: https://api.openai.com/v1
OPENAI_API_KEY: sk-your-production-openai-api-key
```

### 🚀 Deployment Secrets

```bash
# Production API URL
PRODUCTION_API_URL: https://api.resume-enhancer.yourdomain.com

# EC2 SSH Key (Private Key Content)
EC2_SSH_KEY: |
  -----BEGIN OPENSSH PRIVATE KEY-----
  your-private-key-content-here
  -----END OPENSSH PRIVATE KEY-----
```

## Step-by-Step Setup

### 1. Create AWS IAM User

Create a dedicated IAM user for CI/CD with minimal required permissions:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:GetObject",
                "s3:PutObject",
                "s3:DeleteObject",
                "s3:ListBucket"
            ],
            "Resource": [
                "arn:aws:s3:::resume-enhancer-frontend-prod",
                "arn:aws:s3:::resume-enhancer-frontend-prod/*"
            ]
        },
        {
            "Effect": "Allow",
            "Action": [
                "cloudfront:CreateInvalidation"
            ],
            "Resource": "arn:aws:cloudfront::*:distribution/E1234567890123"
        }
    ]
}
```

### 2. Generate SSH Key for EC2

```bash
# Generate new SSH key pair
ssh-keygen -t rsa -b 4096 -f ec2-deploy-key -N ""

# Add public key to EC2 instance
cat ec2-deploy-key.pub >> ~/.ssh/authorized_keys

# Copy private key content for GitHub secret
cat ec2-deploy-key
```

### 3. Configure GitHub Secrets

For each secret listed above:
1. Go to GitHub Repository → Settings → Secrets and variables → Actions
2. Click "New repository secret"
3. Enter the name (e.g., `AWS_ACCESS_KEY_ID`)
4. Paste the value
5. Click "Add secret"

### 4. Verify CI/CD Pipeline

After adding all secrets:
1. Push changes to `main` branch
2. Check GitHub Actions tab
3. Verify all jobs pass:
   - ✅ lint-test-java
   - ✅ lint-test-python  
   - ✅ lint-test-frontend
   - ✅ integration
   - ✅ deploy_frontend
   - ✅ deploy_backend

## Security Best Practices

### 🔐 Secret Management

- **Rotate Regularly**: Change secrets every 90 days minimum
- **Principle of Least Privilege**: Grant minimum required permissions
- **Monitor Usage**: Enable AWS CloudTrail for audit logging
- **Use AWS Secrets Manager**: For production, consider using AWS Secrets Manager instead of environment variables

### 🛡️ Environment Isolation

```bash
# Development secrets (less restrictive)
DEV_AWS_ACCESS_KEY_ID: ...

# Production secrets (most restrictive)
PROD_AWS_ACCESS_KEY_ID: ...
```

### 🚨 Secret Rotation Script

```bash
#!/bin/bash
# rotate-secrets.sh

echo "🔄 Rotating production secrets..."

# 1. Generate new JWT secret
NEW_JWT_SECRET=$(openssl rand -base64 32)
echo "New JWT secret: $NEW_JWT_SECRET"

# 2. Create new AWS access key
aws iam create-access-key --user-name resume-enhancer-ci

# 3. Update GitHub secrets via API
gh secret set JWT_SECRET --body "$NEW_JWT_SECRET"

echo "✅ Secrets rotated successfully"
```

## Troubleshooting

### ❌ Common Issues

**Error: "AWS credentials not found"**
```bash
# Check if secrets are properly set
gh secret list

# Verify AWS credentials
aws sts get-caller-identity
```

**Error: "Permission denied (publickey)"**  
```bash
# Verify EC2 SSH key format
# Ensure private key includes header/footer
# Check EC2 security group allows SSH (port 22)
```

**Error: "OpenAI API quota exceeded"**
```bash
# Check OpenAI billing dashboard
# Verify API key has sufficient credits
# Implement rate limiting in application
```

### 🔍 Debug CI/CD Issues

1. **Check GitHub Actions logs**:
   - Go to Actions tab in repository
   - Click on failed workflow
   - Expand failed step logs

2. **Test secrets locally**:
   ```bash
   # Test AWS credentials
   aws s3 ls s3://resume-enhancer-frontend-prod
   
   # Test EC2 connection
   ssh -i ec2-deploy-key ec2-user@your-ec2-host
   ```

3. **Validate secret values**:
   ```bash
   # Check secret format (without revealing content)
   echo $JWT_SECRET | wc -c  # Should be 44+ characters
   echo $OPENAI_API_KEY | grep "^sk-"  # Should start with sk-
   ```

## Production Checklist

Before enabling automatic deployments:

- [ ] All secrets configured in GitHub
- [ ] AWS IAM permissions tested
- [ ] EC2 SSH access verified
- [ ] OpenAI API key validated
- [ ] S3 bucket and CloudFront distribution exist
- [ ] Domain name and SSL certificates configured
- [ ] Production database and Redis instances ready
- [ ] Monitoring and alerting configured
- [ ] Backup and recovery procedures tested

## Emergency Procedures

### 🚨 Secret Compromise

If any secret is compromised:

1. **Immediately revoke/rotate**:
   ```bash
   # Disable AWS key
   aws iam put-access-key --access-key-id AKIA... --status Inactive
   
   # Generate new JWT secret
   openssl rand -base64 32
   ```

2. **Update GitHub secrets**
3. **Redeploy applications**
4. **Monitor for unauthorized access**

### 🔧 Rollback Deployment

```bash
# SSH to EC2 instance
ssh ec2-user@your-ec2-host

# Rollback to previous version
sudo systemctl stop resume-enhancer-backend
cp /home/ec2-user/backups/resume-enhancer-backend-previous.jar /home/ec2-user/
sudo systemctl start resume-enhancer-backend
```

---

**Remember**: Never commit secrets to version control. Always use secure secret management systems for production environments.
