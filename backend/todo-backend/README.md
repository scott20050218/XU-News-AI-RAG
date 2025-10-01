# Todo Backend - 智能知识管理系统

一个基于 Spring Boot 的智能知识管理系统，支持 RSS 采集、网页爬取、AI 智能处理和 RESTful API 服务。

## 🚀 项目特性

### 核心功能

- **RSS 数据采集**：自动从配置的 RSS 源采集最新内容
- **网页内容爬取**：智能爬取指定网站的内容信息
- **AI 智能处理**：对采集的内容进行深度分析和增强
- **RESTful API**：完整的 CRUD 操作和高级查询功能
- **数据统计**：提供详细的数据统计和分析

### 技术特性

- **Spring Boot 3.2.0**：现代化的 Java 框架
- **Java 21**：使用最新的 Java LTS 版本
- **MySQL 数据库**：生产环境数据存储
- **H2 内存数据库**：测试环境快速验证
- **Spring Data JPA**：简化数据访问层
- **Swagger/OpenAPI**：自动生成 API 文档
- **AI 智能代理**：内容摘要、关键词提取、分类等

## 📋 系统要求

- **Java 21+**
- **Maven 3.6+**
- **MySQL 8.0+**（生产环境）
- **内存**：至少 2GB RAM
- **磁盘**：至少 1GB 可用空间

## 🛠️ 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd backend/todo-backend
```

### 2. 配置数据库

创建 MySQL 数据库：

```sql
CREATE DATABASE todo_backend CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 配置应用

编辑 `src/main/resources/application.properties`：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/todo_backend?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 4. 运行应用

```bash
# 编译项目
mvn clean compile

# 运行应用
mvn spring-boot:run
```

### 5. 访问应用

- **应用地址**：http://localhost:8080
- **API 文档**：http://localhost:8080/swagger-ui.html
- **健康检查**：http://localhost:8080/actuator/health

## 📚 API 文档

### 知识内容管理 API

#### 基础 CRUD 操作

**创建知识内容**

```http
POST /api/knowledge-content
Content-Type: application/json

{
  "title": "文章标题",
  "content": "文章内容",
  "sourceUrl": "https://example.com",
  "contentType": "RSS",
  "tags": "技术,编程",
  "success": true
}
```

**获取知识内容详情**

```http
GET /api/knowledge-content/{id}
```

**更新知识内容**

```http
PUT /api/knowledge-content/{id}
Content-Type: application/json

{
  "title": "更新后的标题",
  "content": "更新后的内容"
}
```

**删除知识内容**

```http
DELETE /api/knowledge-content/{id}
```

#### 高级查询功能

**分页查询**

```http
GET /api/knowledge-content?page=0&size=10&sortBy=acquisitionTime&sortDirection=DESC
```

**条件过滤**

```http
GET /api/knowledge-content?contentType=RSS&processed=true&success=true
```

**关键词搜索**

```http
GET /api/knowledge-content/search?query=Spring Boot&page=0&size=10
```

**标签过滤**

```http
GET /api/knowledge-content?tags=技术,编程
```

#### 批量操作

**批量删除**

```http
POST /api/knowledge-content/batch
Content-Type: application/json

{
  "operation": "DELETE",
  "ids": [1, 2, 3]
}
```

**批量更新状态**

```http
POST /api/knowledge-content/batch
Content-Type: application/json

{
  "operation": "UPDATE_STATUS",
  "ids": [1, 2, 3],
  "processed": true
}
```

**批量添加标签**

```http
POST /api/knowledge-content/batch
Content-Type: application/json

{
  "operation": "ADD_TAGS",
  "ids": [1, 2, 3],
  "tags": "新标签,重要"
}
```

#### 状态管理

**切换处理状态**

```http
PUT /api/knowledge-content/{id}/status?processed=true
```

#### 统计信息

**获取统计信息**

```http
GET /api/knowledge-content/statistics
```

**获取所有标签**

```http
GET /api/knowledge-content/tags
```

## 🤖 AI 智能处理功能

系统集成了 AI 智能代理，对采集的内容进行深度加工：

### 处理功能

- **内容摘要生成**：自动生成内容摘要
- **关键词提取**：智能提取关键词
- **内容分类**：自动分类内容类型
- **情感分析**：分析内容情感倾向
- **质量评估**：评估内容质量
- **语言检测**：自动检测内容语言
- **实体提取**：提取命名实体

### 处理流程

1. 内容预处理和验证
2. 生成智能摘要
3. 提取关键词
4. 内容分类
5. 情感分析
6. 质量评估
7. 语言检测
8. 实体提取
9. 更新处理状态

## 📊 数据采集

### RSS 采集

- **采集频率**：每小时自动执行
- **支持格式**：RSS 2.0, Atom
- **配置源**：Hacker News 等高质量技术资讯

### 网页爬取

- **采集频率**：每 2 小时自动执行
- **支持网站**：InfoQ、博客园等
- **智能解析**：自动提取标题和正文内容

## 🧪 测试

### 运行所有测试

```bash
mvn test
```

### 运行特定测试

```bash
# 运行控制器测试
mvn test -Dtest=KnowledgeContentControllerTest

# 运行服务层测试
mvn test -Dtest=KnowledgeContentServiceTest

# 运行AI处理测试
mvn test -Dtest=AIProcessingServiceTest
```

### 测试覆盖率

- **单元测试**：覆盖所有服务层和控制器
- **集成测试**：验证数据库操作和 API 接口
- **Mock 测试**：隔离外部依赖

## 📁 项目结构

```
src/
├── main/
│   ├── java/cn/lihengrui/todotask/
│   │   ├── controller/          # REST控制器
│   │   ├── service/            # 业务服务层
│   │   ├── repository/         # 数据访问层
│   │   ├── entity/            # 实体类
│   │   ├── dto/               # 数据传输对象
│   │   └── TodoTaskApplication.java
│   └── resources/
│       ├── application.properties    # 应用配置
│       └── db/migration/            # 数据库迁移脚本
└── test/
    ├── java/cn/lihengrui/todotask/
    │   ├── controller/         # 控制器测试
    │   ├── service/           # 服务层测试
    │   └── repository/        # 数据访问层测试
    └── resources/
        └── application-test.properties  # 测试配置
```

## ⚙️ 配置说明

### 应用配置

```properties
# 服务器配置
server.port=8080

# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/todo_backend
spring.datasource.username=root
spring.datasource.password=password

# JPA配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Swagger配置
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html

# Actuator配置
management.endpoints.web.exposure.include=health,info,metrics
```

### 测试配置

```properties
# 测试数据库配置
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.task.scheduling.enabled=false
```

## 🚀 部署

### Docker 部署

```dockerfile
FROM openjdk:21-jdk-slim
COPY target/todo-backend-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 生产环境配置

```bash
# 设置JVM参数
export JAVA_OPTS="-Xms512m -Xmx2048m -XX:+UseG1GC"

# 运行应用
java $JAVA_OPTS -jar todo-backend.jar
```

## 📈 监控和运维

### 健康检查

- **端点**：`/actuator/health`
- **指标**：`/actuator/metrics`
- **信息**：`/actuator/info`

### 日志配置

```properties
# 日志级别配置
logging.level.com.example.todotask=INFO
logging.level.org.springframework.web=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

## 🤝 贡献指南

### 开发流程

1. Fork 项目
2. 创建功能分支
3. 提交代码
4. 创建 Pull Request

### 代码规范

- 使用 Java 21 特性
- 遵循 Spring Boot 最佳实践
- 编写完整的单元测试
- 添加详细的 JavaDoc 注释

## 📄 许可证

本项目采用 MIT 许可证，详情请参阅 LICENSE 文件。

## 📞 联系方式

- **项目维护者**：HA72 开发团队
- **邮箱**：support@example.com
- **问题反馈**：请通过 GitHub Issues 提交

## 🔄 更新日志

### v1.0.0 (2025-09-26)

- ✅ 初始版本发布
- ✅ 实现 RSS 采集功能
- ✅ 实现网页爬取功能
- ✅ 集成 AI 智能处理
- ✅ 完整的 RESTful API
- ✅ 全面的测试覆盖

---

**注意**：本项目仍在积极开发中，如有问题或建议，欢迎提交 Issue 或 Pull Request。
