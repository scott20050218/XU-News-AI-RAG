# HA72 智能知识检索系统

## 项目概述

HA72 是一个基于 Spring Boot + Vue.js 的智能知识检索系统，集成了 LangChain4j 框架和大语言模型，提供智能问答、语义搜索和联网查询功能。

### 核心功能

- **基础知识检索**：基于 FAISS 向量数据库的快速检索
- **增强知识检索**：基于 LangChain4j + OpenAI 的语义搜索
- **联网查询**：当知识库结果不足时，自动触发百度搜索
- **大模型推理**：结合知识库和联网结果进行智能分析
- **邮件通知**：支持导入成功后的邮件通知功能

## 技术架构

### 后端技术栈

- **Spring Boot 3.2.0**：主框架
- **LangChain4j**：大语言模型集成框架
- **OpenAI API**：GPT-3.5-turbo 模型
- **FAISS**：向量数据库（内存版本）
- **Maven**：依赖管理
- **Java 21**：开发语言

### 前端技术栈

- **Vue.js 2.x**：前端框架
- **Element UI**：UI 组件库
- **Axios**：HTTP 客户端
- **Node.js**：运行环境

## 项目结构

```
HA72/
├── backend/                    # 后端项目
│   ├── langchain/             # LangChain 智能检索服务
│   │   ├── src/main/java/     # Java 源码
│   │   ├── src/main/resources/ # 配置文件
│   │   └── pom.xml           # Maven 配置
│   └── ruoyi/                # 主业务系统
├── management/                # 前端项目
│   └── ruoyi-ui/             # Vue.js 前端
├── docs/                     # 项目文档
└── README.md                 # 项目说明
```

## 环境要求

### 系统要求

- **Java**：JDK 21+
- **Node.js**：14.x+
- **Maven**：3.6+
- **内存**：至少 4GB RAM
- **存储**：至少 2GB 可用空间

### 外部依赖

- **OpenAI API Key**：用于大语言模型调用
- **SMTP 服务器**：用于邮件通知（可选）

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/scott20050218/XU-News-AI-RAG/
cd XU-News-AI-RAG
```

### 2. 配置环境变量

创建环境变量文件或设置系统环境变量：

```bash
# OpenAI 配置
export OPENAI_API_KEY="your-openai-api-key"
export OPENAI_BASE_URL="https://api.openai.com/v1"

# 邮件配置（可选）
export MAIL_USERNAME="your-email@gmail.com"
export MAIL_PASSWORD="your-app-password"
export MAIL_FROM="noreply@ha72.com"

# 百度搜索配置（可选）
export BAIDU_API_KEY="your-baidu-api-key"
export BAIDU_SECRET_KEY="your-baidu-secret-key"
```

### 3. 启动后端服务

#### 启动主业务系统（端口 18080）

使用 management/sql 目录下的脚本初始化数据库
数据配置在/management/src/main/resources/application-druid.yml

```bash
cd management
mvn clean package
启动 redis
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

#### 启动 LangChain 智能检索服务（端口 8090）

```bash
cd backend/langchain
export OPENAI_API_KEY="your-openai-api-key"
export OPENAI_BASE_URL="https://api.openai.com/v1"
export MAIL_USERNAME="your-email@gmail.com"
export MAIL_PASSWORD="your-app-password"
export MAIL_FROM="noreply@ha72.com"
export BAIDU_API_KEY="your-baidu-api-key"
export BAIDU_SECRET_KEY="your-baidu-secret-key"
mvn clean package
mvn spring-boot:run
```

#### 启动 数据采集服务（端口 8080）

使用 todo-backend/db.sql 脚本初始化数据库
数据配置在/backend/todo-backend/src/main/resources/application-druid.yml

```bash
cd backend/todo-backend
mvn clean package
mvn spring-boot:run
```

### 4. 启动前端服务

```bash
cd management/ruoyi-ui
npm install
npm run dev
```

前端服务将在 `http://localhost:1024` 启动。

## 详细配置

### 后端配置

#### LangChain 服务配置 (`backend/langchain/src/main/resources/application.properties`)

```properties
# 服务端口
server.port=8090

# OpenAI 配置
openai.api-key=${OPENAI_API_KEY:your-openai-api-key}
openai.base-url=${OPENAI_BASE_URL:https://api.openai.com/v1}
openai.chat-model=gpt-3.5-turbo
openai.embedding-model=text-embedding-ada-002

# 邮件配置
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME:your-email@gmail.com}
spring.mail.password=${MAIL_PASSWORD:your-app-password}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# 联网搜索配置
web.search.enabled=true
web.search.baidu.api-key=${BAIDU_API_KEY:}
web.search.baidu.secret-key=${BAIDU_SECRET_KEY:}
web.search.timeout=5000
web.search.max-results=3
```

### 前端配置

#### 代理配置 (`management/ruoyi-ui/vue.config.js`)

```javascript
module.exports = {
  devServer: {
    proxy: {
      // LangChain API 代理
      "/dev-api/langchain": {
        target: "http://localhost:8090",
        changeOrigin: true,
        pathRewrite: {
          "^/dev-api/langchain": "",
        },
      },
      // 主系统 API 代理
      "/dev-api/api/knowledge-content": {
        target: "http://localhost:8080",
        changeOrigin: true,
        pathRewrite: {
          "^/dev-api": "",
        },
      },
    },
  },
};
```

## 功能要求

1. 实现定时任务机制，通过 RSS、网页抓取及智能代理工具获取新闻信息（需
   遵守网络爬虫规范与安全准则）

- 实现说明

```
 @Scheduled(fixedRate = 3600000) // 每隔1小时执行一次 (3600000毫秒)
    public void acquireRssData() {
        log.info("开始采集 RSS 数据...");
        int successCount = 0;  // 成功采集的条目数量
        int failureCount = 0;  // 失败的RSS源数量
```

```
 private static final List<String> WEB_URLS = List.of(
            "https://www.infoq.cn/news",    // InfoQ 资讯频道
            "https://www.cnblogs.com/"      // 博客园首页
    );
```

2. 基于 Ollama 部署大模型，推荐采用 qwen2.5::3b 模型。

- 实现说明
  基于 openai 的 api 实现，模型使用 GPT-3.5-turbo"

3. 构建本地知识库系统，配置嵌入模型（推荐使用 all-MiniLM-L6-v2）、重排模型
   （推荐使用 ms-marco-MiniLM-L-6-v2）及大语言模型

- 实现说明
  嵌入模型 all-MiniLM-L6-v2，向量维度：384 维
  重排模型 LLM 推理 GPT-3.5-turbo，通过大语言模型进行推理和优化

4. 通过 API 将抓取的信息写入知识库系统，支持结构化数据（可通过 Excel 整理）与非结构化数据类型。

- 实现说明
  通过 API 将抓取的信息写入知识库系统

5. 信息入库成功后，自动发送邮件提示（标题及内容自定义）。

- 实现说明
  信息入库成功后，通过 gmail 邮箱发送邮件。
  （测试环境在德国，163 不允许境外 ip 使用授权码，Hotmail 发送邮件失败）

6. 提供用户登录功能。

- 实现说明
  使用若以框架实现用户权限控制
  ![img.png](./pic/login.jpg)

7. 登录后支持知识库内容管理：查看数据列表（可按类型/时间筛选）、执行单
   条或批量删除操作、编辑元数据（如标签、来源），并支持通过页面上传多种类型
   数据至知识库。

- 实现说明
  ![img.png](./pic/q1.jpg)
  ![img.png](./pic/q2.jpg)
  ![img.png](./pic/q3.jpg)
  ![img.png](./pic/q4.jpg)

8. 登录后提供语义查询功能：基于用户提问优先检索知识库内容，结果按相似度
   AI 行前实战营培训考核项目排序返回。

- 实现说明
  ![img.png](./pic/q5.jpg)
  ![img.png](./pic/q6.jpg)
  ![img.png](./pic/q7.jpg)

9. 9. 若知识库未匹配到相关数据，自动触发联网查询（如调用百度搜索 API），返
      回前 3 条结果经大语言模型推理后输出。

- 实现说明

```
// 2. 判断是否需要联网查询
            boolean needWebSearch = shouldTriggerWebSearch(knowledgeResponse, request);
            List<WebSearchResultDto> webSearchResults = new ArrayList<>();
            long webSearchTime = 0L;
            log.info("needWebSearch: {}", needWebSearch);
            if (needWebSearch) {
                log.info("知识库检索结果不足，触发联网查询: query={}", request.getQuery());

                // 3. 执行联网查询
                long webSearchStart = System.currentTimeMillis();
                List<WebSearchService.WebSearchResult> webResults =
                    webSearchService.searchBaidu(request.getQuery(), 3);
```

10. 登录后提供知识库数据聚类分析报告，展示关键词 Top10 分布

- 实现说明
  ![img.png](./pic/q8.jpg)
  ![img.png](./pic/q9.jpg)

## 技术要求

1. 技术栈选型：前端采用 React 或 Vue 框架，后端采用 SpringBoot 或 Flask 框架，关系型数据库选用 MySQL 或 SQLite，向量数据库采用 FAISS。

- 实现说明
  技术栈选型：前端采用 Vue 框架，后端采用 SpringBoot 框架，关系型数据库选用 MySQL，向量数据库采用 FAISS。

2. 数据存储设计：元数据（如数据 ID、数据类型等）存储于关系型数据库，向量数据单独存储于 FAISS 向量数据库，实现数据分类管理与高效检索。

- 实现说明
  数据存储设计：元数据（如数据 ID、数据类型等）存储于关系型数据库，向量数据单独存储于 FAISS 向量数据库，实现数据分类管理与高效检索。

3. 框架集成：核心业务逻辑开发采用 LangChain 框架，支撑知识库构建与检索增强功能。

- 实现说明
  框架集成：核心业务逻辑开发采用 LangChain 框架，支撑知识库构建与检索增强功能。

4. 模型调用：通过标准化 API 接口实现与大模型服务的交互，确保服务调用的规范性与可扩展性。

- 实现说明
  模型调用：通过标准化 API 接口实现与大模型服务的交互，确保服务调用的规范性与可扩展性。

5. 身份认证：登录功能优先采用 Spring Security 框架或 JWT 技术方案，保障用户身份验证的安全性与可靠性。

- 实现说明
  身份认证：登录功能优先采用 Spring Security 框架，保障用户身份验证的安全性与可靠性。

## 其他要求

1. 项目资料提交要求：将本项目全部相关资料上传至 GitHub 仓库，仓库命名为“xu-ai-news-rag”。

- 实现说明
  GitHub 仓库，https://github.com/scott20050218/XU-News-AI-RAG/

2. 需提供产品需求文档（PRD）。

- 实现说明
  ./产品需求文档.md

3. 需提供概要设计文档与技术架构文档

- 实现说明
  ./设计文档.md

4. 需提供产品原型设计文件。

- 实现说明
  /prototype/my-app
  文档见：/prototype/my-app/README.md，/prototype/my-app/项目说明文档.md

5. 需提供前端与后端完整代码。

- 实现说明
  前端代码见：/mannagement/ruoyi-ui
  后端代码见：/backend/todo-backend,/backend/langchain, /mannagement

6. 需提供单元测试、集成测试及 API 测试相关代码。

- 实现说明
  /backend/todo-backend 下，mvn test
  /backend/langchain 下，mvn test

7. 若项目涉及关系型数据库操作，需提供对应的 SQL 语句。

- 实现说明
  /backend/todo-backend/src/main/resources/sql
  /mannagement/ruoyi-ui/src/main/resources/sql

8. 需提供项目说明文档（README.md），明确项目部署、运行及使用方式。

- 实现说明
  参考本 README.md 和各个工程下 README.md

9. 可选：准备项目介绍文档进行技术分享。

- 实现说明
  参考 log.md record.md

## API 接口说明

### 增强搜索接口

**接口地址**：`POST /api/enhanced-knowledge/enhanced-search`

**请求参数**：

```json
{
  "query": "德国国庆节放假几天",
  "topK": 5
}
```

**响应示例**：

```json
{
  "code": 200,
  "data": {
    "query": "德国国庆节放假几天",
    "knowledgeResults": [...],
    "webSearchResults": [
      {
        "title": "德国国庆节放假安排 - 2025年最新官方信息",
        "url": "https://www.baidu.com/link?url=example1",
        "snippet": "德国国庆节(统一日)是10月3日，这是德国的法定节假日，通常放假1天。",
        "source": "百度搜索API(模拟)"
      }
    ],
    "llmInference": "基于知识库和联网搜索结果...",
    "webSearchTriggered": true,
    "searchMethod": "知识库+联网+LLM",
    "totalProcessingTimeMs": 28
  }
}
```

### 其他接口

- **基础知识检索**：`POST /api/enhanced-knowledge/search`
- **智能问答**：`POST /api/enhanced-knowledge/ask`
- **知识内容管理**：`GET /api/knowledge-content`

## 使用指南

### 1. 知识库管理

1. 访问 `http://localhost:1024`
2. 登录系统（默认账号：admin/admin123）
3. 进入"知识管理" → "知识内容"
4. 添加、编辑或删除知识内容

### 2. 智能搜索

1. 进入"知识检索"页面
2. 选择搜索模式：
   - **基础搜索**：快速检索知识库内容
   - **增强搜索**：语义搜索 + 联网查询 + AI 分析
   - **智能问答**：直接与 AI 对话
3. 输入查询内容，点击搜索

### 3. 联网查询功能

系统会自动判断是否需要联网查询：

- 知识库结果数量不足
- 搜索结果相似度较低
- 用户查询涉及最新信息

联网查询会调用百度搜索 API，并整合结果进行 AI 分析。

## 部署说明

### 开发环境部署

按照"快速开始"章节的步骤进行部署。

### 生产环境部署

#### 后端部署

1. **编译打包**：

```bash
cd backend/langchain
mvn clean package -DskipTests
```

2. **运行 JAR 包**：

```bash
java -jar target/langchain-0.0.1-SNAPSHOT.jar
```

#### 前端部署

1. **构建生产版本**：

```bash
cd management/ruoyi-ui
npm run build
```

2. **部署到 Web 服务器**：
   将 `dist` 目录下的文件部署到 Nginx 或 Apache 服务器。

#### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /path/to/ruoyi-ui/dist;
        try_files $uri $uri/ /index.html;
    }

    # API 代理
    location /dev-api/langchain/ {
        proxy_pass http://localhost:8090/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /dev-api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 故障排除

### 常见问题

#### 1. OpenAI API 调用失败

**症状**：搜索时提示 API 调用失败
**解决方案**：

- 检查 `OPENAI_API_KEY` 环境变量是否正确设置
- 确认 API Key 有足够的额度
- 检查网络连接是否正常

#### 2. 前端 404 错误

**症状**：前端调用 API 返回 404
**解决方案**：

- 确认后端服务正在运行（端口 8090）
- 检查 `vue.config.js` 中的代理配置
- 确认 API 路径配置正确

#### 3. 邮件发送失败

**症状**：邮件通知功能不工作
**解决方案**：

- 检查 SMTP 服务器配置
- 确认邮箱授权码正确
- 检查网络防火墙设置

#### 4. 联网查询不工作

**症状**：搜索时没有触发联网查询
**解决方案**：

- 检查 `web.search.enabled` 配置
- 确认百度 API 密钥配置（可选，系统有模拟模式）
- 调整联网查询触发阈值

### 日志查看

#### 后端日志

```bash
# LangChain 服务日志
tail -f backend/langchain/logs/langchain-ai-service.log

# 主系统日志
tail -f backend/ruoyi/logs/sys-info.log
```

#### 前端日志

在浏览器开发者工具的 Console 面板查看前端日志。

## 性能优化

### 后端优化

1. **JVM 参数调优**：

```bash
java -Xms2g -Xmx4g -XX:+UseG1GC -jar langchain-0.0.1-SNAPSHOT.jar
```

2. **数据库连接池配置**：

```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

### 前端优化

1. **代码分割**：使用路由懒加载
2. **资源压缩**：启用 Gzip 压缩
3. **缓存策略**：配置静态资源缓存

## 开发指南

### 添加新的搜索源

1. 在 `WebSearchService` 中添加新的搜索方法
2. 更新 `EnhancedKnowledgeSearchService` 的搜索逻辑
3. 添加相应的配置项

### 自定义 AI 模型

1. 修改 `LangChainService` 中的模型配置
2. 更新 `application.properties` 中的模型参数
3. 重新启动服务

### 扩展知识库

1. 在知识管理页面添加新内容
2. 系统会自动向量化并加入搜索索引
3. 支持批量导入功能

## 贡献指南

1. Fork 本项目
2. 创建特性分支：`git checkout -b feature/new-feature`
3. 提交更改：`git commit -am 'Add new feature'`
4. 推送分支：`git push origin feature/new-feature`
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证。详情请参阅 [LICENSE](LICENSE) 文件。

## 联系方式

- **项目维护者**：HA72 团队
- **邮箱**：support@ha72.com
- **文档**：https://docs.ha72.com

## 更新日志

### v1.0.0 (2025-09-30)

- ✅ 基础知识检索功能
- ✅ 增强语义搜索
- ✅ 联网查询集成
- ✅ 大模型推理
- ✅ 邮件通知功能
- ✅ 完整的 Web 界面

---

**注意**：请确保在生产环境中使用前，仔细阅读并理解所有配置项，并根据实际需求进行相应的安全配置。
