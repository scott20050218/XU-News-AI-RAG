# LangChain 项目 API 接口文档

## 项目概述

LangChain 项目是一个基于 Spring Boot 和 LangChain4j 框架的智能知识检索和问答系统。它提供了丰富的 REST API 接口，支持文本向量化、知识库检索、智能问答等功能。

**基本信息:**

- 项目名称: LangChain 智能知识检索系统
- 版本: 1.0.0
- 基础 URL: `http://localhost:8090`
- 开发团队: HA72 开发团队
- 技术栈: Spring Boot 3.2.0, LangChain4j 0.34.0, Java 21

## API 接口概览

系统提供三大类 API 接口：

1. **增强知识检索** (`/api/enhanced-knowledge`) - 基于 LangChain4j 和大模型的智能检索
2. **基础知识检索** (`/api/knowledge`) - 传统向量检索和问答
3. **向量化处理** (`/api/vectorization`) - 数据向量化和存储管理

## 1. 增强知识检索 API

### 1.1 LangChain 智能检索

**接口地址:** `POST /api/enhanced-knowledge/search`

**功能描述:** 使用 LangChain4j 框架进行智能知识检索，支持语义理解和上下文分析。

**请求参数:**

```json
{
  "query": "string", // 必需，查询文本
  "topK": 10, // 可选，返回结果数量，默认10
  "minSimilarity": 0.0, // 可选，最小相似度阈值，默认0.0
  "contentType": "string", // 可选，内容类型过滤
  "processedOnly": false // 可选，是否只返回已处理内容，默认false
}
```

**响应示例:**

```json
{
  "success": true,
  "message": "LangChain检索完成",
  "code": 200,
  "method": "LangChain4j",
  "data": {
    "query": "博客园",
    "results": [
      {
        "title": "博客园 - 开发者的网上家园",
        "content": "博客园内容摘要...",
        "summary": "博客园是面向开发者的专业社区",
        "similarity": 0.95,
        "sourceUrl": "https://www.cnblogs.com",
        "acquisitionTime": "2025-09-26T21:28:19",
        "tagArray": ["技术", "开发", "社区"]
      }
    ],
    "resultCount": 1,
    "processingTimeMs": 150,
    "vectorizationTimeMs": 0,
    "searchTimeMs": 50,
    "timestamp": "2025-09-27T00:18:21",
    "averageSimilarity": 0.95,
    "maxSimilarity": 0.95,
    "minSimilarity": 0.95
  }
}
```

### 1.2 大模型智能问答

**接口地址:** `POST /api/enhanced-knowledge/ask`

**功能描述:** 使用大语言模型进行智能问答，结合检索到的知识内容生成准确答案。

**请求参数:**

```json
{
  "question": "什么是博客园？" // 必需，用户问题
}
```

**响应示例:**

```json
{
  "success": true,
  "message": "大模型问答完成",
  "code": 200,
  "method": "LLM",
  "question": "什么是博客园？",
  "answer": "基于知识库内容，我为您找到以下相关信息：\n\n博客园是一个面向开发者的专业技术社区..."
}
```

### 1.3 处理知识内容到 LangChain

**接口地址:** `POST /api/enhanced-knowledge/process-langchain`

**功能描述:** 将知识内容处理并添加到 LangChain 向量存储中。

**请求参数:**

- `page` (query 参数): 页码，默认 0
- `size` (query 参数): 页面大小，默认 10

**响应示例:**

```json
{
  "success": true,
  "message": "LangChain向量存储处理完成",
  "code": 200,
  "method": "LangChain4j",
  "processedCount": 1,
  "page": 0,
  "size": 10
}
```

### 1.4 获取 LangChain 服务状态

**接口地址:** `GET /api/enhanced-knowledge/status`

**功能描述:** 获取 LangChain 服务的运行状态和配置信息。

**响应示例:**

```json
{
  "success": true,
  "message": "LangChain服务状态正常",
  "code": 200,
  "data": {
    "langchainEnabled": true,
    "embeddingModel": "AllMiniLmL6V2",
    "vectorStore": "InMemoryEmbeddingStore",
    "chatModel": "OpenAI GPT-3.5-turbo",
    "vectorCount": 0
  }
}
```

### 1.5 获取详细向量存储信息

**接口地址:** `GET /api/enhanced-knowledge/debug/vector-info`

**功能描述:** 获取 AllMiniLmL6V2 嵌入模型的详细存储信息，用于调试和监控。

**响应示例:**

```json
{
  "success": true,
  "message": "获取详细向量存储信息成功",
  "code": 200,
  "data": {
    "embeddingModel": "AllMiniLmL6V2",
    "modelType": "本地嵌入模型",
    "vectorDimension": 384,
    "vectorCount": 0,
    "vectorStore": "InMemoryEmbeddingStore",
    "storageType": "内存存储",
    "persistenceEnabled": true,
    "modelStatus": "已加载",
    "initializationTime": "应用启动时"
  }
}
```

## 2. 基础知识检索 API

### 2.1 知识库检索 (POST)

**接口地址:** `POST /api/knowledge/search`

**功能描述:** 基于用户提问检索知识库内容，结果按相似度排序返回。

**请求参数:**

```json
{
  "query": "string", // 必需，查询文本
  "topK": 10, // 可选，返回结果数量，默认10
  "minSimilarity": 0.0, // 可选，最小相似度阈值，默认0.0
  "contentType": "string", // 可选，内容类型过滤
  "processedOnly": false // 可选，是否只返回已处理内容，默认false
}
```

### 2.2 快速知识检索 (GET)

**接口地址:** `GET /api/knowledge/search`

**功能描述:** 基于 GET 请求的快速知识检索接口，适合简单查询。

**请求参数:**

- `query` (必需): 查询文本
- `topK` (可选): 返回结果数量，默认 10
- `minSimilarity` (可选): 最小相似度阈值，默认 0.0
- `contentType` (可选): 内容类型过滤
- `processedOnly` (可选): 是否只返回已处理内容，默认 false

**示例请求:**

```
GET /api/knowledge/search?query=博客园&topK=5&minSimilarity=0.3
```

### 2.3 智能问答

**接口地址:** `POST /api/knowledge/ask`

**功能描述:** 基于知识库的智能问答接口，提供结构化答案。

**请求参数:**

```json
{
  "question": "什么是博客园？" // 必需，用户问题
}
```

**响应示例:**

```json
{
  "success": true,
  "message": "问答完成",
  "code": 200,
  "question": "什么是博客园？",
  "answer": "基于知识库内容，我为您找到以下相关信息：\n\n1. 博客园 - 开发者的网上家园\n   摘要：博客园是面向开发者的专业社区\n   相似度：0.95\n",
  "sources": [...],
  "confidence": 0.95,
  "processingTime": 150
}
```

## 3. 向量化处理 API

### 3.1 处理所有知识内容向量化

**接口地址:** `POST /api/vectorization/process-all`

**功能描述:** 从 API 获取所有知识内容并进行向量化处理。

**响应示例:**

```json
{
  "success": true,
  "message": "处理完成",
  "processedCount": 100
}
```

### 3.2 处理指定页面知识内容向量化

**接口地址:** `POST /api/vectorization/process-page`

**功能描述:** 处理指定页面的知识内容向量化。

**请求参数:**

- `page` (query 参数): 页码，从 0 开始，默认 0
- `size` (query 参数): 页面大小，默认 100

### 3.3 处理单个知识内容向量化

**接口地址:** `POST /api/vectorization/process-single/{id}`

**功能描述:** 处理指定 ID 的知识内容向量化。

**路径参数:**

- `id`: 知识内容 ID

### 3.4 搜索相似内容

**接口地址:** `GET /api/vectorization/search`

**功能描述:** 根据查询文本搜索相似的知识内容。

**请求参数:**

- `query` (必需): 查询文本
- `topK` (可选): 返回结果数量，默认 10

### 3.5 获取向量存储统计信息

**接口地址:** `GET /api/vectorization/stats`

**功能描述:** 获取向量存储的统计信息。

**响应示例:**

```json
{
  "success": true,
  "message": "获取统计信息成功",
  "stats": {
    "vectorCount": 100,
    "apiConnectionStatus": true,
    "storageType": "FAISS",
    "lastUpdated": "2025-09-27T00:18:21"
  }
}
```

### 3.6 向量存储管理

#### 清空向量存储

**接口地址:** `DELETE /api/vectorization/clear`

#### 保存向量存储

**接口地址:** `POST /api/vectorization/save`

#### 加载向量存储

**接口地址:** `POST /api/vectorization/load`

## 数据模型

### KnowledgeSearchRequest

```json
{
  "query": "string", // 查询文本
  "topK": 10, // 返回结果数量
  "minSimilarity": 0.0, // 最小相似度阈值
  "contentType": "string", // 内容类型过滤
  "processedOnly": false // 是否只返回已处理内容
}
```

### KnowledgeSearchResponse

```json
{
  "query": "string",                    // 查询文本
  "results": [...],                     // 检索结果列表
  "resultCount": 0,                     // 结果数量
  "processingTimeMs": 0,                // 总处理时间
  "vectorizationTimeMs": 0,             // 向量化时间
  "searchTimeMs": 0,                    // 搜索时间
  "timestamp": "2025-09-27T00:18:21",   // 响应时间戳
  "averageSimilarity": 0.0,             // 平均相似度
  "maxSimilarity": 0.0,                 // 最高相似度
  "minSimilarity": 0.0                  // 最低相似度
}
```

### KnowledgeSearchResult

```json
{
  "title": "string", // 标题
  "content": "string", // 内容
  "summary": "string", // 摘要
  "similarity": 0.0, // 相似度
  "sourceUrl": "string", // 来源URL
  "acquisitionTime": "2025-09-27T00:18:21", // 采集时间
  "tagArray": ["tag1", "tag2"] // 标签数组
}
```

## 错误处理

所有 API 接口都遵循统一的错误响应格式：

```json
{
  "success": false,
  "message": "错误描述",
  "code": 400,
  "data": null
}
```

**常见错误码:**

- `400`: 请求参数错误
- `404`: 资源不存在
- `500`: 服务器内部错误

## 快速开始

### 1. 启动服务

```bash
cd backend/langchain
mvn spring-boot:run
```

服务将在 `http://localhost:8090` 启动。

### 2. 健康检查

```bash
curl http://localhost:8090/actuator/health
```

### 3. 处理知识内容

```bash
# 处理知识内容到LangChain向量存储
curl -X POST "http://localhost:8090/api/enhanced-knowledge/process-langchain?page=0&size=10"
```

### 4. 执行智能检索

```bash
# LangChain智能检索
curl -X POST "http://localhost:8090/api/enhanced-knowledge/search" \
  -H "Content-Type: application/json" \
  -d '{"query": "博客园", "topK": 5}'
```

### 5. 智能问答

```bash
# 大模型问答
curl -X POST "http://localhost:8090/api/enhanced-knowledge/ask" \
  -H "Content-Type: application/json" \
  -d '{"question": "什么是博客园？"}'
```

## 系统监控

### Swagger UI

访问 `http://localhost:8090/swagger-ui.html` 查看完整的 API 文档和测试界面。

### Actuator 端点

- 健康检查: `GET /actuator/health`
- 系统信息: `GET /actuator/info`

## 性能优化建议

1. **批量处理**: 使用 `/api/vectorization/process-page` 进行批量向量化
2. **缓存策略**: 系统内置向量缓存，减少重复计算
3. **分页查询**: 大数据量检索时使用 `topK` 参数控制返回结果数量
4. **过滤条件**: 使用 `contentType` 和 `processedOnly` 参数缩小检索范围

## 技术特性

- **向量化模型**: AllMiniLmL6V2 (384 维本地嵌入模型)
- **向量存储**: InMemoryEmbeddingStore (内存存储)
- **大语言模型**: OpenAI GPT-3.5-turbo (可选)
- **检索算法**: 余弦相似度计算
- **持久化**: 支持向量数据的保存和加载

## 注意事项

1. **模型配置**: 确保本地嵌入模型已正确加载
2. **内存管理**: 大量数据向量化时注意内存使用
3. **API 限制**: OpenAI API 需要配置有效的 API Key
4. **网络连接**: 确保与 todo-backend (端口 8080) 的网络连接正常

---

**最后更新**: 2025 年 9 月 27 日  
**联系方式**: HA72 开发团队  
**项目仓库**: [GitHub 链接]
