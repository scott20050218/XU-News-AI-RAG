# LangChain 知识库智能检索与问答系统

## 项目概述

LangChain 知识库智能检索与问答系统是一个基于 Spring Boot 和 LangChain4j 的智能知识库应用，提供文本向量化、语义检索和智能问答功能。该系统能够从外部 API 获取知识内容，使用先进的嵌入模型进行向量化处理，并基于大语言模型提供智能问答服务。

## 核心特性

### 🚀 主要功能

- **智能向量化**: 使用 LangChain4j 和 AllMiniLmL6V2 嵌入模型进行文本向量化
- **语义检索**: 基于向量相似度的智能知识检索
- **智能问答**: 集成 OpenAI GPT-3.5-turbo 的 RAG (检索增强生成) 问答系统
- **RESTful API**: 完整的 REST API 接口，支持 Swagger 文档
- **数据持久化**: 向量数据持久化存储，支持应用重启后数据恢复
- **健康监控**: 集成 Spring Boot Actuator 进行系统监控

### 🧠 AI 能力

- **嵌入模型**: AllMiniLmL6V2 (本地) / OpenAI Embeddings (云端)
- **聊天模型**: OpenAI GPT-3.5-turbo
- **向量存储**: InMemoryEmbeddingStore (内存向量数据库)
- **相似度计算**: 余弦相似度算法
- **RAG 架构**: 检索增强生成，结合知识库检索和大模型生成

## 技术架构

### 技术栈

- **框架**: Spring Boot 3.2.0
- **Java 版本**: Java 21
- **构建工具**: Maven
- **AI 框架**: LangChain4j 0.34.0
- **嵌入模型**: AllMiniLmL6V2 / OpenAI Embeddings
- **聊天模型**: OpenAI GPT-3.5-turbo
- **HTTP 客户端**: Spring WebFlux
- **API 文档**: SpringDoc OpenAPI (Swagger)
- **监控**: Spring Boot Actuator
- **测试**: JUnit 5 + Mockito

### 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Todo-Backend  │    │   LangChain     │    │   OpenAI API    │
│   (数据源)       │◄───┤   (向量化服务)   │◄───┤   (AI 模型)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  知识内容 API    │    │  向量存储        │    │  智能问答       │
│  /api/knowledge │    │  InMemoryStore  │    │  RAG 生成       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 核心组件

#### 1. 服务层 (Service Layer)

- **LangChainService**: LangChain4j 核心服务，处理嵌入和聊天模型
- **EnhancedKnowledgeSearchService**: 增强型知识检索服务
- **ApiClientService**: 外部 API 客户端服务
- **VectorizationService**: 向量化处理服务
- **FaissVectorStore**: 自定义向量存储服务

#### 2. 控制器层 (Controller Layer)

- **EnhancedKnowledgeSearchController**: 增强型知识检索 API
- **KnowledgeSearchController**: 基础知识检索 API
- **VectorizationController**: 向量化处理 API

#### 3. 数据传输对象 (DTO)

- **KnowledgeContentDto**: 知识内容数据传输对象
- **KnowledgeSearchRequest**: 知识检索请求
- **KnowledgeSearchResponse**: 知识检索响应
- **ApiResponseDto**: 通用 API 响应

## 快速开始

### 环境要求

- Java 21+
- Maven 3.6+
- 网络连接 (用于访问 OpenAI API)

### 安装步骤

1. **克隆项目**

```bash
git clone <repository-url>
cd backend/langchain
```

2. **配置环境变量**

```bash
# 设置 OpenAI API Key (可选，如果不设置将使用本地模型)
export OPENAI_API_KEY=your_openai_api_key_here
```

3. **编译项目**

```bash
mvn clean compile
```

4. **运行测试**

```bash
mvn test
```

5. **启动应用**

```bash
mvn spring-boot:run
```

### 配置说明

#### application.properties 配置

```properties
# 服务器配置
server.port=8090

# Todo-backend API 配置
todo-backend.api.url=http://localhost:8080

# 向量化配置
vectorization.dimension=384
vectorization.batch-size=100

# LangChain4j 配置
langchain.openai.api-key=${OPENAI_API_KEY:}
langchain.openai.model=gpt-3.5-turbo
langchain.embedding.model=all-minilm-l6-v2

# 日志配置
logging.level.cn.lihengrui.langchain=INFO
```

## API 文档

### 基础向量化 API

#### 1. 处理知识内容到向量存储

```http
POST /api/vectorization/process
Content-Type: application/json

{
  "page": 0,
  "size": 10
}
```

#### 2. 向量化搜索

```http
POST /api/vectorization/search
Content-Type: application/json

{
  "query": "什么是博客园？",
  "topK": 5
}
```

#### 3. 获取向量存储统计

```http
GET /api/vectorization/stats
```

### 增强型知识检索 API

#### 1. LangChain 语义检索

```http
POST /api/enhanced-knowledge/search
Content-Type: application/json

{
  "query": "技术博客平台",
  "topK": 5,
  "minSimilarity": 0.0,
  "contentType": null,
  "includeTags": false
}
```

#### 2. 智能问答

```http
POST /api/enhanced-knowledge/ask?question=什么是博客园？&topK=3
```

#### 3. 处理知识内容到 LangChain

```http
POST /api/enhanced-knowledge/process-langchain?page=0&size=10
```

#### 4. 获取 LangChain 服务状态

```http
GET /api/enhanced-knowledge/status
```

#### 5. 清空 LangChain 向量存储

```http
DELETE /api/enhanced-knowledge/clear-langchain
```

### 基础知识检索 API

#### 1. 知识检索 (POST)

```http
POST /api/knowledge/search
Content-Type: application/json

{
  "query": "技术博客",
  "topK": 5,
  "minSimilarity": 0.0,
  "contentType": null,
  "includeTags": false
}
```

#### 2. 知识检索 (GET)

```http
GET /api/knowledge/search?query=技术博客&topK=5
```

#### 3. 智能问答

```http
POST /api/knowledge/ask?question=什么是博客园？&topK=3
```

## 使用示例

### 1. 基本使用流程

#### 步骤 1: 启动应用

```bash
mvn spring-boot:run
```

#### 步骤 2: 处理知识内容

```bash
curl -X POST "http://localhost:8090/api/enhanced-knowledge/process-langchain?page=0&size=10"
```

#### 步骤 3: 进行语义检索

```bash
curl -X POST "http://localhost:8090/api/enhanced-knowledge/search" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "技术博客平台",
    "topK": 5
  }'
```

#### 步骤 4: 智能问答

```bash
curl -X POST "http://localhost:8090/api/enhanced-knowledge/ask?question=什么是博客园？&topK=3"
```

### 2. 高级使用

#### 使用不同的嵌入模型

```properties
# 使用 OpenAI 嵌入模型
langchain.embedding.model=openai

# 使用本地 AllMiniLmL6V2 模型
langchain.embedding.model=all-minilm-l6-v2
```

#### 自定义向量化参数

```properties
# 向量维度
vectorization.dimension=384

# 批处理大小
vectorization.batch-size=100
```

## 数据存储

### 向量数据存储位置

- **内存存储**: `InMemoryEmbeddingStore` (运行时)
- **持久化文件**:
  - `faiss_vector_store.ser` - 向量索引数据
  - `faiss_content_store.ser` - 内容数据

### 数据持久化机制

- 应用启动时自动加载已保存的向量数据
- 应用关闭时自动保存向量数据到文件
- 支持增量更新和批量处理

## 监控和健康检查

### 健康检查端点

```http
GET /actuator/health
```

### 应用信息

```http
GET /actuator/info
```

### Swagger API 文档

```http
GET /swagger-ui.html
```

## 测试

### 运行所有测试

```bash
mvn test
```

### 运行特定测试类

```bash
mvn test -Dtest=LangChainServiceTest
mvn test -Dtest=EnhancedKnowledgeSearchServiceTest
mvn test -Dtest=EnhancedKnowledgeSearchControllerTest
```

### 测试覆盖率

项目包含完整的单元测试，覆盖：

- 服务层测试 (Service Tests)
- 控制器层测试 (Controller Tests)
- 集成测试 (Integration Tests)

## 性能优化

### 向量化性能

- 使用批处理进行向量化处理
- 支持异步处理大量数据
- 内存优化的向量存储

### 检索性能

- 高效的相似度计算算法
- 可配置的检索参数
- 智能的结果排序

## 故障排除

### 常见问题

#### 1. OpenAI API 连接失败

```
错误: OpenAI API 连接失败
解决: 检查 OPENAI_API_KEY 环境变量是否正确设置
```

#### 2. 向量存储加载失败

```
错误: 向量存储文件损坏
解决: 删除 faiss_vector_store.ser 和 faiss_content_store.ser 文件，重新处理数据
```

#### 3. 内存不足

```
错误: OutOfMemoryError
解决: 增加 JVM 堆内存: -Xmx2g -Xms1g
```

### 日志配置

```properties
# 启用详细日志
logging.level.cn.lihengrui.langchain=DEBUG
logging.level.dev.langchain4j=DEBUG
```

## 开发指南

### 项目结构

```
src/
├── main/
│   ├── java/cn/lihengrui/langchain/
│   │   ├── controller/          # REST 控制器
│   │   ├── dto/                 # 数据传输对象
│   │   ├── service/             # 业务服务层
│   │   └── LangchainApplication.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/cn/lihengrui/langchain/
        ├── controller/          # 控制器测试
        └── service/             # 服务测试
```

### 添加新功能

1. 在 `service` 包中添加新的服务类
2. 在 `controller` 包中添加新的控制器
3. 在 `dto` 包中添加新的数据传输对象
4. 添加相应的单元测试

### 代码规范

- 使用 Lombok 减少样板代码
- 遵循 Spring Boot 最佳实践
- 编写完整的 JavaDoc 注释
- 保持代码简洁和可读性

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

- 项目维护者: HA72 开发团队
- 邮箱: [your-email@example.com]
- 项目链接: [https://github.com/your-username/langchain](https://github.com/your-username/langchain)

## 更新日志

### v1.0.0 (2025-09-26)

- 初始版本发布
- 集成 LangChain4j 框架
- 实现智能向量化和检索功能
- 添加 OpenAI GPT-3.5-turbo 集成
- 完整的 REST API 接口
- 全面的单元测试覆盖

---

**注意**: 本项目需要 OpenAI API Key 才能使用完整的 AI 功能。如果不设置 API Key，系统将使用本地嵌入模型进行向量化处理。
