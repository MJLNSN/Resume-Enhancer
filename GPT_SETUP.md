# GPT 功能配置指南

## 🔑 OpenAI API 配置

Resume Enhancer的AI增强功能需要OpenAI API支持。以下是详细的配置步骤：

### 1. 获取 OpenAI API Key

1. 访问 [OpenAI Platform](https://platform.openai.com/)
2. 注册/登录账户
3. 进入 [API Keys 页面](https://platform.openai.com/account/api-keys)
4. 点击 "Create new secret key" 生成新的API密钥
5. **重要**: 复制并妥善保存密钥（只显示一次）

### 2. 本地开发配置

创建环境变量文件：

```bash
# 复制模板文件
cp .env.template .env

# 编辑环境变量
nano .env
```

在 `.env` 文件中设置：

```bash
# OpenAI 配置 - 必须设置以启用GPT功能
OPENAI_API_KEY=sk-your-actual-api-key-here
OPENAI_API_BASE=https://api.openai.com/v1
OPENAI_MODEL=gpt-3.5-turbo
OPENAI_MAX_TOKENS=2000
OPENAI_TEMPERATURE=0.3
```

### 3. 功能说明

配置完成后，以下AI功能将可用：

#### 🎯 简历增强 (Rewrite)
- **输入**: 原始简历文本 + 目标职位描述（可选）
- **输出**: 专业化、简洁、结果导向的简历内容
- **特点**: 保留事实信息（公司、日期、指标）

#### 🧠 AI建议生成 (Suggestions)  
- **输入**: 解析后的简历JSON + 目标职位
- **输出**: 3-5条实用改进建议
- **内容**: 技能学习、项目想法、成果量化、行业经验等

#### 🌏 智能翻译 (Translation)
- **功能**: 中英文互译
- **特点**: 保留日期、数字、技术名词
- **输出**: 保持格式的Markdown内容

### 4. 成本控制

为控制API调用成本，系统设有以下保护：

- **默认模式**: 优先使用本地模板
- **Token限制**: 单次请求最大2000 tokens
- **智能缓存**: 相同内容不重复调用
- **用户选择**: 用户可选择本地或GPT模式

### 5. 故障排除

#### 常见问题：

**Q: GPT功能不可用？**
A: 检查以下设置：
- 确认 `OPENAI_API_KEY` 已正确设置
- 验证API key有效且有余额
- 检查网络连接

**Q: API调用失败？**
A: 可能原因：
- API key无效或过期
- 超出使用限额
- 网络连接问题
- API服务临时不可用

**Q: 响应质量不佳？**  
A: 优化建议：
- 调整 `OPENAI_TEMPERATURE` (0.1-0.5)
- 增加 `OPENAI_MAX_TOKENS` 值
- 提供更详细的职位描述

### 6. 安全注意事项

⚠️ **重要安全提醒**：

- **绝不提交** `.env` 文件到版本控制
- 定期更换 API key
- 监控 API 使用量和账单
- 使用环境变量管理敏感信息

### 7. 生产环境配置

在生产环境中：

```bash
# 使用环境变量或密钥管理服务
export OPENAI_API_KEY="your-production-api-key"
export OPENAI_MODEL="gpt-4"  # 可选择更高级模型
```

---

## 🎉 开始体验

配置完成后：

1. 重启服务：`./stop.sh && ./start.sh`
2. 上传简历并解析完成
3. 点击 "Enhance Resume" 
4. 选择 "GPT Powered" 模式
5. 享受AI驱动的简历优化！

有问题？查看日志：`tail -f logs/backend.log`
