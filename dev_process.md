# Resume Enhancer 开发最佳顺序（Step-by-Step）

## **阶段 0：准备环境**

1. **版本管理**
   * 初始化 Git 仓库，建议 Monorepo：`/frontend`, `/backend`, `/nlp`。
   * 配置 `.gitignore`：排除 `.env`、node\_modules、Python 虚拟环境。
2. **开发环境**

   * 安装 Docker + docker-compose。
   * 配置 PostgreSQL（本地容器）+ MinIO（模拟 S3）。
   * 前端：Node 18+, npm/yarn。
   * 后端：Java 17+ (Spring Boot), Python 3.11+。
3. **CI/CD 配置**

   * 简单 GitHub Actions pipeline，保证后续推送可自动 lint/build。

---

## **阶段 1：核心基础设施**

1. **数据库模型**

   * 创建 `users`, `resumes`, `enhanced_resumes`, `jobs`, `match_results` 表。
   * 测试本地连接，保证 CRUD 可用。
2. **存储服务**

   * 集成 MinIO/S3 SDK。
   * 能够上传文件，返回可访问 URL。
3. **用户认证**

   * JWT 登录/注册。
   * 确保密码哈希、Token 生成、鉴权中间件可用。

---

## **阶段 2：简历上传与文本抽取**

1. **前端上传组件**

   * 支持 PDF/TXT。
   * 显示上传进度。
2. **后端上传处理**

   * Java 接收 multipart 文件。
   * 验证用户和文件大小。
   * 上传至 S3。
   * 返回 `resumeId` 给前端。
3. **PDF → Text 异步处理**

   * Python NLP Service 或 Java 库。
   * 异常处理：parse\_error → 允许 GPT fallback。
   * 存储 raw\_text 至数据库。

---

## **阶段 3：简历解析（结构化）**

1. **Local NLP**

   * Python FastAPI 提供 `/parse` 接口。
   * 返回 JSON：`{skills, experiences, education}`。
2. **Java 调用解析**

   * 保存至 `resumes.parsed_json`。
   * 超时/失败 → 标记 parse\_error。
3. **前端展示解析结果**

   * 树状 / 列表展示，用户可编辑。
   * MVP：只显示基本技能/经历/教育信息。

---

## **阶段 4：增强（Rewrite）**

1. **本地模板增强（MVP）**

   * 用 NLP rules + 简单模板生成“简历优化版本”。
   * 返回 Markdown。
2. **GPT 增强**

   * 构建 prompt：`Rewrite` + `Job Focus`。
   * 解析 GPT 返回 JSON → 存 `enhanced_resumes`。
   * fallback 本地模板。
3. **前端对比功能**

   * 原文 vs 增强。
   * 支持复制 / 导出 Markdown。

---

## **阶段 5：建议生成（Suggestions）**

1. **GPT 提示生成**

   * 输入 `{parsed_json, jobDescription}` → 输出 JSON array。
2. **存储与展示**

   * 保存到 `enhanced_resumes.suggestions`。
   * 前端显示 3–5 条可操作建议。

---

## **阶段 6：翻译（Translate）**

1. **GPT 翻译接口**

   * 保留年份、数字、技术名词。
   * 输出 Markdown。
2. **多语言版本管理**

   * `enhanced_resumes.language` 标记。
   * 支持原文 / 英文 / 中文切换。

---

## **阶段 7：导出与版本管理**

1. **Markdown → PDF**

   * 使用 wkhtmltopdf 或 headless Chromium。
2. **S3 存储下载链接**

   * 前端可点击下载。
3. **版本对比**

   * 展示不同增强版本 / 翻译版本。

---

## **阶段 8：前端完善**

1. **Dashboard**

   * 显示用户简历列表、版本、分数（可选）。
2. **异步状态提示**

   * 上传 / 解析 / 增强 / 翻译 → loading / error。
3. **权限与安全**

   * 仅用户可访问自己的简历。

---

## **阶段 9：CI/CD 与部署**

1. **GitHub Actions**

   * Java / Python / Frontend lint → build → test。
   * 可选部署：EC2 + Lambda + S3。
2. **Secrets 管理**

   * OPENAI\_API\_KEY, AWS\_ACCESS\_KEY, JWT\_SECRET。
3. **成本控制**

   * 缓存 GPT 结果。
   * 用户每日调用次数限制。

---

## **阶段 10：安全与优化**

1. **日志最小化**

   * GPT 调用仅记录次数，raw\_response 可脱敏。
2. **上传限制**

   * PDF/TXT，max 5MB。
3. **性能优化**

   * 异步队列处理 PDF 解析和 GPT 调用。
   * 前端 lazy load / virtual list。

---

### ✅ 开发顺序总结（精简）

```
0. 环境准备 → Git / Docker / 本地服务
1. 数据库 + 存储 + 用户认证
2. 简历上传 + PDF→Text
3. 简历解析（NLP JSON）
4. 简历增强（本地模板 → GPT）
5. 建议生成（Suggestions）
6. 翻译功能
7. 导出 Markdown/PDF + 版本管理
8. 前端完善（Dashboard + 状态显示）
9. CI/CD + 部署
10. 安全 / 性能优化
```

------




