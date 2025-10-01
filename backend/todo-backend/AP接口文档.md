# Todo-Backend API 接口文档

## 📋 项目概述

**Todo-Backend** 是一个基于 Spring Boot 的知识内容管理系统，提供完整的 RESTful API 服务。系统支持知识内容的采集、存储、处理和检索，集成了 AI 智能代理进行内容深度加工。

### 🏗️ 技术栈

- **框架**: Spring Boot 3.2.0
- **数据库**: MySQL 8.0
- **ORM**: Spring Data JPA + Hibernate
- **API 文档**: SpringDoc OpenAPI (Swagger)
- **构建工具**: Maven
- **Java 版本**: Java 21

### 🌐 基础信息

- **服务地址**: `http://localhost:8080`
- **API 文档**: `http://localhost:8080/swagger-ui.html`
- **健康检查**: `http://localhost:8080/actuator/health`

---

## 📚 API 接口总览

### 🎯 核心功能模块

| 模块             | 描述                          | 接口数量 |
| ---------------- | ----------------------------- | -------- |
| **知识内容管理** | CRUD 操作、状态管理、批量处理 | 8 个     |
| **搜索与过滤**   | 关键词搜索、条件过滤          | 2 个     |
| **统计分析**     | 内容统计、数据概览            | 1 个     |
| **系统监控**     | 健康检查、应用信息            | 2 个     |

### 📊 接口统计

- **总接口数**: 13 个
- **GET 接口**: 6 个
- **POST 接口**: 3 个
- **PUT 接口**: 2 个
- **DELETE 接口**: 2 个

---

## 🔧 通用响应格式

### ✅ 成功响应

```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    /* 具体数据 */
  },
  "timestamp": "2025-09-27T00:00:00"
}
```

### ❌ 错误响应

```json
{
  "success": false,
  "message": "错误描述",
  "error": "详细错误信息",
  "timestamp": "2025-09-27T00:00:00"
}
```

### 📄 分页响应

```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "content": [
      /* 数据列表 */
    ],
    "totalElements": 100,
    "totalPages": 10,
    "size": 20,
    "number": 0,
    "first": true,
    "last": false,
    "numberOfElements": 20,
    "empty": false
  }
}
```

---

## 📖 详细接口文档

### 1. 知识内容管理 API

#### 1.1 获取知识内容列表

**接口描述**: 获取知识内容列表，支持分页、排序和多种过滤条件

**请求信息**:

- **URL**: `GET /api/knowledge-content`
- **Content-Type**: `application/json`

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| `page` | int | 否 | 0 | 页码（从 0 开始） |
| `size` | int | 否 | 20 | 每页大小 |
| `sort` | string | 否 | acquisitionTime | 排序字段 |
| `direction` | string | 否 | DESC | 排序方向（ASC/DESC） |
| `contentType` | string | 否 | - | 内容类型过滤（RSS/Web/Manual） |
| `processed` | boolean | 否 | - | 处理状态过滤 |
| `success` | boolean | 否 | - | 成功状态过滤 |
| `keyword` | string | 否 | - | 关键词搜索 |
| `tags` | string | 否 | - | 标签过滤 |

**请求示例**:

```bash
GET /api/knowledge-content?page=0&size=10&sort=acquisitionTime&direction=DESC&contentType=RSS&processed=true
```

**响应示例**:

```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Spring Boot 最佳实践",
        "summary": "本文介绍了Spring Boot开发的最佳实践...",
        "sourceUrl": "https://example.com/article1",
        "contentType": "RSS",
        "acquisitionTime": "2025-09-27T10:00:00",
        "processed": true,
        "success": true,
        "tagArray": ["Spring", "Java", "最佳实践"],
        "contentLength": 1500
      }
    ],
    "totalElements": 50,
    "totalPages": 5,
    "size": 10,
    "number": 0,
    "first": true,
    "last": false,
    "numberOfElements": 10,
    "empty": false
  }
}
```

#### 1.2 获取单个知识内容

**接口描述**: 根据 ID 获取单个知识内容的详细信息

**请求信息**:

- **URL**: `GET /api/knowledge-content/{id}`
- **Content-Type**: `application/json`

**路径参数**:
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| `id` | long | 是 | 知识内容 ID |

**请求示例**:

```bash
GET /api/knowledge-content/1
```

**响应示例**:

```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "id": 1,
    "title": "Spring Boot 最佳实践",
    "content": "Spring Boot是一个用于构建Java应用程序的框架...",
    "sourceUrl": "https://example.com/article1",
    "contentType": "RSS",
    "acquisitionTime": "2025-09-27T10:00:00",
    "tags": "Spring,Java,最佳实践",
    "processed": true,
    "success": true,
    "errorMessage": null,
    "summary": "Spring Boot是一个用于构建Java应用程序的框架...",
    "tagArray": ["Spring", "Java", "最佳实践"],
    "contentLength": 1500
  }
}
```

#### 1.3 创建知识内容

**接口描述**: 创建新的知识内容

**请求信息**:

- **URL**: `POST /api/knowledge-content`
- **Content-Type**: `application/json`

**请求体**:

```json
{
  "title": "新文章标题",
  "content": "文章内容...",
  "sourceUrl": "https://example.com/article",
  "contentType": "Manual",
  "tags": "标签1,标签2,标签3",
  "processed": false,
  "success": true,
  "errorMessage": null
}
```

**字段说明**:
| 字段名 | 类型 | 必填 | 长度限制 | 描述 |
|--------|------|------|----------|------|
| `title` | string | 是 | 1-500 字符 | 内容标题 |
| `content` | string | 否 | 最大 50000 字符 | 内容正文 |
| `sourceUrl` | string | 是 | 最大 2000 字符 | 来源 URL |
| `contentType` | string | 是 | - | 内容类型（RSS/Web/Manual） |
| `tags` | string | 否 | 最大 1000 字符 | 标签（逗号分隔） |
| `processed` | boolean | 否 | - | 处理状态 |
| `success` | boolean | 是 | - | 成功状态 |
| `errorMessage` | string | 否 | 最大 1000 字符 | 错误信息 |

**响应示例**:

```json
{
  "success": true,
  "message": "创建成功",
  "data": {
    "id": 2,
    "title": "新文章标题",
    "content": "文章内容...",
    "sourceUrl": "https://example.com/article",
    "contentType": "Manual",
    "acquisitionTime": "2025-09-27T12:00:00",
    "tags": "标签1,标签2,标签3",
    "processed": false,
    "success": true,
    "errorMessage": null,
    "summary": "文章内容...",
    "tagArray": ["标签1", "标签2", "标签3"],
    "contentLength": 100
  }
}
```

#### 1.4 更新知识内容

**接口描述**: 更新现有的知识内容

**请求信息**:

- **URL**: `PUT /api/knowledge-content/{id}`
- **Content-Type**: `application/json`

**路径参数**:
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| `id` | long | 是 | 知识内容 ID |

**请求体**: 与创建接口相同

**响应示例**:

```json
{
  "success": true,
  "message": "更新成功",
  "data": {
    "id": 1,
    "title": "更新后的标题",
    "content": "更新后的内容...",
    "sourceUrl": "https://example.com/article",
    "contentType": "Manual",
    "acquisitionTime": "2025-09-27T10:00:00",
    "tags": "新标签1,新标签2",
    "processed": true,
    "success": true,
    "errorMessage": null,
    "summary": "更新后的内容...",
    "tagArray": ["新标签1", "新标签2"],
    "contentLength": 200
  }
}
```

#### 1.5 删除知识内容

**接口描述**: 删除指定的知识内容

**请求信息**:

- **URL**: `DELETE /api/knowledge-content/{id}`
- **Content-Type**: `application/json`

**路径参数**:
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| `id` | long | 是 | 知识内容 ID |

**响应示例**:

```json
{
  "success": true,
  "message": "删除成功",
  "data": null
}
```

#### 1.6 切换处理状态

**接口描述**: 切换知识内容的处理状态

**请求信息**:

- **URL**: `PUT /api/knowledge-content/{id}/status`
- **Content-Type**: `application/json`

**路径参数**:
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| `id` | long | 是 | 知识内容 ID |

**查询参数**:
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| `processed` | boolean | 是 | 新的处理状态 |

**请求示例**:

```bash
PUT /api/knowledge-content/1/status?processed=true
```

**响应示例**:

```json
{
  "success": true,
  "message": "状态更新成功",
  "data": {
    "id": 1,
    "title": "Spring Boot 最佳实践",
    "content": "Spring Boot是一个用于构建Java应用程序的框架...",
    "sourceUrl": "https://example.com/article1",
    "contentType": "RSS",
    "acquisitionTime": "2025-09-27T10:00:00",
    "tags": "Spring,Java,最佳实践",
    "processed": true,
    "success": true,
    "errorMessage": null,
    "summary": "Spring Boot是一个用于构建Java应用程序的框架...",
    "tagArray": ["Spring", "Java", "最佳实践"],
    "contentLength": 1500
  }
}
```

#### 1.7 批量操作

**接口描述**: 对多个知识内容进行批量操作

**请求信息**:

- **URL**: `POST /api/knowledge-content/batch`
- **Content-Type**: `application/json`

**请求体**:

```json
{
  "ids": [1, 2, 3, 4, 5],
  "operation": "DELETE",
  "parameter": null,
  "force": true
}
```

**字段说明**:
| 字段名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| `ids` | array | 是 | 要操作的 ID 列表 |
| `operation` | string | 是 | 操作类型（DELETE/UPDATE_STATUS/ADD_TAGS/REMOVE_TAGS） |
| `parameter` | string | 否 | 操作参数（根据操作类型而定） |
| `force` | boolean | 否 | 是否强制执行（默认 false） |

**支持的操作类型**:

- `DELETE`: 批量删除
- `UPDATE_STATUS`: 批量更新状态
- `ADD_TAGS`: 批量添加标签
- `REMOVE_TAGS`: 批量移除标签

**响应示例**:

```json
{
  "success": true,
  "message": "批量删除操作成功，影响 5 条记录",
  "data": "批量删除操作成功，影响 5 条记录"
}
```

### 2. 搜索与过滤 API

#### 2.1 搜索知识内容

**接口描述**: 根据关键词搜索知识内容

**请求信息**:

- **URL**: `GET /api/knowledge-content/search`
- **Content-Type**: `application/json`

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| `query` | string | 是 | - | 搜索关键词 |
| `page` | int | 否 | 0 | 页码 |
| `size` | int | 否 | 20 | 每页大小 |

**请求示例**:

```bash
GET /api/knowledge-content/search?query=Spring Boot&page=0&size=10
```

**响应示例**:

```json
{
  "success": true,
  "message": "搜索成功",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Spring Boot 最佳实践",
        "summary": "本文介绍了Spring Boot开发的最佳实践...",
        "sourceUrl": "https://example.com/article1",
        "contentType": "RSS",
        "acquisitionTime": "2025-09-27T10:00:00",
        "processed": true,
        "success": true,
        "tagArray": ["Spring", "Java", "最佳实践"],
        "contentLength": 1500
      }
    ],
    "totalElements": 3,
    "totalPages": 1,
    "size": 10,
    "number": 0,
    "first": true,
    "last": true,
    "numberOfElements": 3,
    "empty": false
  }
}
```

### 3. 统计分析 API

#### 3.1 获取统计信息

**接口描述**: 获取知识内容的统计信息

**请求信息**:

- **URL**: `GET /api/knowledge-content/statistics`
- **Content-Type**: `application/json`

**响应示例**:

```json
{
  "success": true,
  "message": "获取统计信息成功",
  "data": {
    "totalCount": 100,
    "rssCount": 60,
    "webCount": 30,
    "manualCount": 10,
    "processedCount": 80,
    "unprocessedCount": 20,
    "successCount": 95,
    "failedCount": 5,
    "todayCount": 15,
    "yesterdayCount": 12,
    "contentTypes": {
      "RSS": 60,
      "Web": 30,
      "Manual": 10
    },
    "processingStats": {
      "processed": 80,
      "unprocessed": 20
    },
    "successStats": {
      "success": 95,
      "failed": 5
    }
  }
}
```

### 4. 系统监控 API

#### 4.1 健康检查

**接口描述**: 检查应用健康状态

**请求信息**:

- **URL**: `GET /actuator/health`
- **Content-Type**: `application/json`

**响应示例**:

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 250000000000,
        "threshold": 10485760,
        "exists": true
      }
    }
  }
}
```

#### 4.2 应用信息

**接口描述**: 获取应用基本信息

**请求信息**:

- **URL**: `GET /actuator/info`
- **Content-Type**: `application/json`

**响应示例**:

```json
{
  "app": {
    "name": "todo-backend",
    "description": "知识内容管理系统",
    "version": "0.0.1-SNAPSHOT"
  },
  "build": {
    "version": "0.0.1-SNAPSHOT",
    "artifact": "todo-backend",
    "name": "todo-backend",
    "group": "cn.lihengrui",
    "time": "2025-09-27T00:00:00.000Z"
  }
}
```

---

## 🔍 数据模型

### KnowledgeContent 实体

| 字段名            | 类型          | 描述     | 约束                   |
| ----------------- | ------------- | -------- | ---------------------- |
| `id`              | Long          | 主键 ID  | 自增                   |
| `title`           | String        | 内容标题 | 必填，最大 500 字符    |
| `content`         | String        | 内容正文 | 最大 50000 字符        |
| `sourceUrl`       | String        | 来源 URL | 必填，最大 2000 字符   |
| `contentType`     | String        | 内容类型 | 必填（RSS/Web/Manual） |
| `acquisitionTime` | LocalDateTime | 采集时间 | 自动生成               |
| `tags`            | String        | 标签信息 | 最大 1000 字符         |
| `processed`       | Boolean       | 处理状态 | 默认 false             |
| `success`         | Boolean       | 成功状态 | 必填                   |
| `errorMessage`    | String        | 错误信息 | 最大 1000 字符         |

### 内容类型说明

| 类型     | 描述             | 来源         |
| -------- | ---------------- | ------------ |
| `RSS`    | RSS 源采集的内容 | RSS 订阅源   |
| `Web`    | 网页爬取的内容   | 网页爬虫     |
| `Manual` | 手动添加的内容   | 用户手动输入 |

---

## ⚠️ 错误码说明

| HTTP 状态码 | 错误类型              | 描述           | 解决方案                   |
| ----------- | --------------------- | -------------- | -------------------------- |
| `400`       | Bad Request           | 请求参数错误   | 检查请求参数格式和必填字段 |
| `404`       | Not Found             | 资源不存在     | 检查资源 ID 是否正确       |
| `500`       | Internal Server Error | 服务器内部错误 | 查看服务器日志，联系管理员 |

### 常见错误示例

**参数验证错误**:

```json
{
  "success": false,
  "message": "参数验证失败",
  "error": "标题不能为空",
  "timestamp": "2025-09-27T00:00:00"
}
```

**资源不存在**:

```json
{
  "success": false,
  "message": "资源不存在",
  "error": "ID为999的知识内容不存在",
  "timestamp": "2025-09-27T00:00:00"
}
```

---

## 🚀 快速开始

### 1. 环境准备

```bash
# 确保MySQL服务运行
mysql -u root -p

# 创建数据库
CREATE DATABASE todotask;
```

### 2. 启动应用

```bash
# 进入项目目录
cd backend/todo-backend

# 启动应用
mvn spring-boot:run
```

### 3. 访问 API 文档

打开浏览器访问: `http://localhost:8080/swagger-ui.html`

### 4. 测试 API

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 获取内容列表
curl http://localhost:8080/api/knowledge-content

# 创建新内容
curl -X POST http://localhost:8080/api/knowledge-content \
  -H "Content-Type: application/json" \
  -d '{
    "title": "测试文章",
    "content": "这是测试内容",
    "sourceUrl": "https://example.com",
    "contentType": "Manual",
    "success": true
  }'
```

---

## 📝 更新日志

### v1.0.0 (2025-09-27)

- ✅ 完成基础 CRUD API
- ✅ 实现分页和过滤功能
- ✅ 添加批量操作支持
- ✅ 集成 Swagger API 文档
- ✅ 实现搜索功能
- ✅ 添加统计分析 API
- ✅ 集成 Spring Boot Actuator 监控

---

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

---

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

---

## 📞 联系方式

- **项目团队**: HA72 开发团队
- **邮箱**: support@ha72.com
- **项目地址**: https://github.com/ha72/todo-backend

---

_最后更新: 2025-09-27_
