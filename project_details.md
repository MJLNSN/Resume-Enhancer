
# Resume Enhancer

> **一句话说明**：一个 AI 驱动的 Resume Enhancer 平台，自动解析并增强简历（改写、翻译、个性化提升建议），保存不同版本并可部署到 AWS。适合作为求职者的作品演示与实战项目。

---

## 最终效果（用户可见）

- 用户注册/登录 → 上传简历（PDF 或纯文本）。
- 系统提取文本并生成结构化解析（技能、项目、教育、时间线）。
- 用户选择目标岗位并执行“增强”或“翻译”操作：
  - **增强（Rewrite）**：GPT 输出更专业、简洁、结果导向的简历段落；并保存为一版 ‘Enhanced’ 简历。
  - **翻译（Translate）**：中⇄英翻译，保留技术名词与时间数字。
  - **提升建议（Suggestions）**：基于岗位给出 3–5 条可操作性提升建议（学习内容 / 加强项目 / 量化成果方向）。
- 用户可以对比原文/增强/翻译版本、导出 Markdown/PDF，并在简历中直接复制建议句子。

---

## 核心功能一览

- 简历上传与存储（S3）
- 文本抽取（PDF → text）
- 本地 NLP 解析（Python FastAPI）或 GPT 解析（可选）
- GPT 驱动的增强 / 翻译 / 建议生成
- 结果持久化（Postgres）并支持版本对比
- 管理面板：调用统计、开关 GPT 模式、配额管理
- CI/CD：GitHub Actions 自动测试与部署

---

## 高层架构
```

Frontend (React + TS)
 |
 |--- API Gateway (Java Spring Boot)
 |
 |--- NLP Service (Python FastAPI)
 |--- GPT Service (Proxy API)
 |--- Database (Postgres on AWS RDS)
 |--- Storage (AWS S3)

```
---

## 技术栈

- 前端：React + TypeScript (Vite)
- 后端主入口：Java (Spring Boot, Maven)
- NLP 服务：Python (FastAPI, spaCy / transformers)
- LLM：调用第三方 GPT 转发代理（OPENAI_API_BASE 可配置）
- DB：PostgreSQL（jsonb 用于解析/建议）
- 存储：AWS S3
- 部署：EC2 (Java), Lambda (Python 可选), RDS, S3
- CI/CD：GitHub Actions

---

## 数据模型（简化版）

```sql
users(id PK, email, password_hash, created_at)
resumes(id PK, user_id FK, file_url, raw_text, created_at)
enhanced_resumes(id PK, resume_id FK, enhanced_text, language ENUM, suggestions jsonb, created_at)
jobs(id PK, user_id FK, title, description)
match_results(id PK, resume_id FK, job_id FK, score float, matched_skills jsonb, created_at)
```

------

## 关键 API（契约速览）

> 所有接口前缀：/api/v1

- POST /auth/register — body {email,password} → {userId, token}
- POST /auth/login — body {email,password} → {token}
- POST /resumes/upload — multipart file → {resumeId, s3Url}
- GET /resumes/{id} → {id, s3Url, raw_text, parsed_json}
- POST /analyze — body {resumeId, jobDescription, mode: 'local'|'gpt', forceRefresh:false} → returns enhanced result + score
- POST /translate — body {resumeId?, text?, targetLang:'en'|'zh', mode:'gpt'|'local'} → {translated_text}
- GET /enhanced/{id} — 获取某个增强版本

------

## 功能 → 后端详细逻辑

### 1. 上传并抽取文本

- 前端上传 → Java 接收 → 验证用户/大小 → 上传 S3 → 异步抽取 PDF→text → 存入 resumes.raw_text
- 异步处理，抽取失败可回退至 GPT 或手动输入
- 使用 pdfminer.six / pdfplumber 或 Java PDF 库

### 2. 解析（local NLP）

- Java 调 Python /parse → 返回结构化 JSON {skills:[], experiences:[], education:[]}
- 异常处理：parse_error → 提供 GPT-assisted parse

### 3. 增强（Rewrite）

- 用户选择岗位 / 关键词 → Java 检查缓存 → 调 GPT 或 local template → 存入 enhanced_resumes
- GPT 无效 JSON → 重试 n 次 → fallback local template

### 4. 建议生成（Suggestions）

- 结合 (parsed_json + jobDescription) 调 GPT
- 返回 {'suggestions': [...]} → 存入 enhanced_resumes

### 5. 翻译（Translate）

- 保留年份、数字、技术名词
- 翻译结果存 enhanced_resumes（language 标注）

### 6. 导出（Markdown / PDF）

- Markdown 转 PDF: wkhtmltopdf / headless Chromium
- 上传生成物到 S3 → 返回下载链接

------

## GPT Prompts

**增强（Rewrite）**:

```
You are an expert career coach. Rewrite the following resume excerpt to be concise, professional, and results-oriented. Preserve facts (companies, dates, metrics) and output the enhanced text in Markdown. Input: <<resume_text>>. Job focus: <<job_description>>.
```

**提升建议（Suggestions）**:

```
Given resume: <<resume_json>> and job: <<job_description>>, suggest 3-5 practical ways the candidate can improve competitiveness. Return ONLY JSON: {'suggestions': ['...','...']}.
```

**翻译（Translate）**:

```
Translate the following resume into [English/Chinese], preserving dates, numbers, and technology names. Output Markdown.
```

**解析（GPT-assisted parse）**:

```
Extract skills, experiences, education from the following resume text and return JSON: {'skills':[],'experiences':[{'company':'','title':'','years':'','bullets':[]}],'education':[]}
```

------

## 部署建议

- S3：存储原始简历与生成物，前端可用 presigned URL 上传
- EC2 (Java)：t2/t3.micro（Free Tier）
- RDS (Postgres)：Free Tier micro 实例
- Python NLP：轻量模型用 Lambda，重模型用 EC2
- 成本控制：缓存结果、限制每天 GPT 调用次数、默认 local NLP

------

## 本地开发快速开始

1. 克隆仓库 → cp .env.example .env → 填 local DB / MinIO 配置
2. docker-compose up --build (Postgres + MinIO + Java + Python)
3. 前端：cd frontend && npm install && npm run dev
4. 访问：http://localhost:3000 → 上传 demo resume，测试 Enhance/Translate

------

## CI/CD（GitHub Actions）

- Jobs: lint-test-java, lint-test-python, lint-test-frontend, integration, deploy_frontend, deploy_backend
- Secrets：AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, OPENAI_API_BASE, OPENAI_API_KEY, EC2_SSH_KEY, JWT_SECRET

------

## 安全与隐私

- 用户必须知情：简历可能发送至第三方 GPT 转发服务
- 不上传身份证/护照等敏感信息
- 日志最小化，raw_response 可短期保存或脱敏

------

## 开发注意事项对照表

| 模块         | 注意事项           | 说明                           | 执行建议 / 检查点                                |
| ------------ | ------------------ | ------------------------------ | ------------------------------------------------ |
| 配置管理     | 配置文件集中管理   | 所有敏感信息、环境变量不硬编码 | `.env` 或 `config.yaml/json`，dev/test/prod 分离 |
|              | 前端环境变量       | 区分 API 地址、模式            | `.env.development`, `.env.production`            |
| 安全         | 用户认证           | 避免明文密码                   | JWT / OAuth2，密码哈希存储                       |
|              | 文件上传安全       | 限制类型、大小                 | PDF/TXT，max 5MB，S3 presigned URL               |
|              | GPT 调用安全       | 防滥用 & 日志脱敏              | 速率限制，日志不记录完整文本                     |
| 模块化       | 前后端分离         | React / Java / Python 独立     | 模块化目录，易升级/替换                          |
|              | GPT 调用封装       | 可替换 LLM API                 | 单独 service 层封装 GPT 调用逻辑                 |
|              | 数据库灵活         | JSON/JSONB 保存解析/建议       | 多版本管理，避免覆盖原始数据                     |
| 错误处理     | GPT 返回校验       | 避免 JSON/格式错误             | JSON schema 校验，fallback 本地模板              |
|              | NLP 异常处理       | PDF 转文本失败                 | 标记 parse_error → 重试 GPT                      |
| 日志与监控   | 日志最小化         | 避免敏感信息泄露               | GPT 调用仅记录次数，异常单独日志                 |
| 成本控制     | GPT 优先本地 NLP   | 降低调用成本                   | 默认 local NLP，用户主动选择 GPT                 |
|              | AWS Free Tier 控制 | EC2/RDS/S3 预算                | t2.micro, RDS micro, S3 ≤5GB                     |
| 开发流程     | Docker & Compose   | 管理依赖环境                   | Postgres + MinIO + Java + Python 容器化          |
|              | GitHub Actions     | CI/CD 自动化                   | lint-test-build-deploy pipeline                  |
| 用户体验     | 异步状态           | 上传/解析/增强显示状态         | 前端进度条、加载状态、错误提示                   |
|              | 翻译保格式         | 保留原排版                     | 年份、数字、技术名词不翻译                       |
| GPT 调用策略 | Prompt 管理        | 易修改与调优                   | Prompt 模板统一管理                              |
|              | fallback 机制      | GPT 无响应处理                 | 返回 local 模板或提示用户稍后重试                |
| 本地开发     | S3 模拟            | 离线测试                       | 使用 MinIO，接口同 S3                            |
|              | 后端隔离调试       | 快速迭代                       | Java / Python 服务独立启动                       |
|              | 数据库容器化       | 本地清理方便                   | Docker Postgres                                  |
| 版本控制     | 仓库结构           | 易管理多模块                   | Monorepo 或子目录                                |
|              | 密钥管理           | 防止泄露                       | `.env` / gitignore                               |
|              | 文档更新           | Prompt、API、README            | 每次改动同步更新                                 |
| 非技术       | 项目目标明确       | 避免功能泛化                   | 简历增强 + 提升建议 + 翻译                       |
|              | 功能迭代顺序       | 保证 MVP 可用                  | 上传 → 解析 → 增强 → 翻译 → 建议 → 导出          |
|              | 隐私告知           | 用户知情                       | 提示简历可能经过 GPT 转发 API                    |

------


```
Built an AI-powered Resume Enhancer: full-stack app using Java (Spring Boot) and Python (FastAPI) for NLP, React frontend, PostgreSQL (AWS RDS), S3 storage, integrated GPT APIs for rewrite/translation/suggestions, and CI/CD via GitHub Actions.

```
