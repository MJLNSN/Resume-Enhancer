# 🚀 Resume Enhancer - Quick Start Guide

## 快速开始

### 一键启动（推荐）
```bash
# 1. 进入项目目录
cd /home/mjl/111job/project

# 2. 一键启动所有服务
./start.sh
```

等待30-60秒后，访问 **http://localhost:3000** 开始使用！

### 停止服务
```bash
./stop.sh
```

### 测试服务状态
```bash
./test-services.sh
```

---

## 🎯 核心功能演示

### 1. 用户注册/登录
- 访问 http://localhost:3000
- 点击注册创建新账户
- 或使用现有账户登录

### 2. 上传简历
- 在Dashboard点击"Upload Resume"
- 拖拽或点击上传PDF/TXT文件（最大5MB）
- 系统自动提取文本并解析

### 3. 查看解析结果
- 点击简历卡片查看详情
- 查看提取的文本内容
- 查看解析的结构化信息：
  - 技能列表
  - 工作经历
  - 教育背景

### 4. AI增强功能 ⭐️
- 点击"Enhance Resume"按钮
- 选择本地模板或GPT模式
- 可输入目标职位描述进行定向优化
- 获得专业化的简历重写

### 5. AI建议生成 🧠
- 自动生成3-5条改进建议
- 包含技能学习、项目加强、成果量化等
- 基于目标职位的个性化建议

### 6. 多语言翻译 🌏
- 一键中英文互译
- 保留技术名词和时间数字
- 适合海外求职需求

### 7. 导出与对比 📄
- 导出Markdown格式（即时下载）
- 导出PDF格式（云端生成）
- 版本对比功能（原文vs增强版）
- 支持多个增强版本管理

---

## 🔧 技术架构

### 服务端口
- **前端**: http://localhost:3000 (React + TypeScript)
- **后端API**: http://localhost:8080/api/v1 (Java Spring Boot)
- **NLP服务**: http://localhost:8000 (Python FastAPI)
- **数据库**: PostgreSQL (端口5432)
- **文件存储**: MinIO (端口9000, 管理界面9001)

### 核心技术栈
- **前端**: React 18 + TypeScript + Vite + Tailwind CSS
- **后端**: Java 17 + Spring Boot 3.1 + Spring Security + JWT
- **NLP**: Python 3.11 + FastAPI + spaCy
- **数据库**: PostgreSQL + JSONB
- **存储**: MinIO (S3兼容)

---

## 📝 项目状态

### ✅ 已完成功能
- ✅ 用户注册/登录 (JWT认证)
- ✅ 简历文件上传 (PDF/TXT, 5MB限制)
- ✅ PDF文本提取 (Apache PDFBox)
- ✅ 智能简历解析 (Python spaCy + 规则引擎)
- ✅ 结构化数据展示 (技能/经历/教育)
- ✅ **GPT驱动的简历增强** (专业化重写)
- ✅ **AI建议生成** (3-5条改进建议)
- ✅ **中英文翻译** (保留技术名词)
- ✅ **Markdown/PDF导出** (云端PDF生成)
- ✅ **版本对比功能** (原文vs增强版)
- ✅ **增强版本管理** (多版本追踪)
- ✅ 响应式前端界面
- ✅ 异步处理状态显示
- ✅ 安全文件存储 (S3兼容)

### 🚧 未来扩展方向
- 更多语言翻译支持
- 行业特定的简历模板
- 批量简历处理
- 简历评分系统
- 求职建议知识库
- AWS生产环境部署优化

**当前所有核心功能已完成，系统功能齐全！** 🎉

---

## 🛠 开发模式

### 手动启动服务（开发调试）
```bash
# 启动基础设施
docker-compose up -d

# 启动Python NLP服务
cd nlp
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --reload --port 8000 &

# 启动Java后端
cd ../backend
./mvnw spring-boot:run &

# 启动React前端  
cd ../frontend
npm install
npm run dev &
```

### 查看日志
```bash
# 服务日志
tail -f logs/nlp.log      # NLP服务日志
tail -f logs/backend.log  # 后端服务日志
tail -f logs/frontend.log # 前端服务日志

# Docker日志
docker-compose logs -f postgres  # 数据库日志
docker-compose logs -f minio     # 存储服务日志
```

---

## 🎉 开始使用

现在你可以：

1. **运行 `./start.sh`** 启动所有服务
2. **访问 http://localhost:3000** 注册账户
3. **上传你的简历** 体验AI解析功能
4. **查看结构化结果** 了解系统能力

**项目已经可以在本地完整运行！** 🚀

需要帮助？查看 `DEVELOPMENT.md` 了解详细的开发信息。
