# 定时任务应用后端

基于 Java Spring Boot 3.0 的 RESTful 待办事项管理 API，支持完整的 CRUD、状态切换、批量删除、分页、过滤、健康检查、Swagger 文档等。

## 功能描述

- 首选 RSS 获取数据
- 其次网页抓取，需遵守网络爬虫规范与安全准则
- 在获取信息的基础上，利用智能代理对信息进行深度加工，提升信息价值，处理后的数据用数据库保存

## 技术栈

- Java 21+
- Spring Boot 3.0
- Spring Data JPA
- MySQL 5.7+
- Lombok
- Swagger (springdoc-openapi)

## 数据库配置

- 地址：localhost:3306
- 用户名：root
- 密码：123456
- 数据库名：todotask

## 测试策略

1. **单元测试**:
2. **集成测试**: API 接口测试
3. **E2E 测试**:
4. **性能测试**: 大量数据场景测试
