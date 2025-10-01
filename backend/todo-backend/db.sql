-- ================================================================
-- Todo-Backend 数据库脚本
-- 
-- 该脚本用于创建Todo-Backend智能知识管理系统的数据库结构
-- 包含数据库创建、表结构定义、索引创建和初始数据插入
-- 
-- 数据库：MySQL 8.0+
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_unicode_ci
-- 
-- 作者：HA72开发团队
-- 版本：1.0
-- 创建时间：2025-09-26
-- ================================================================

-- ================================================================
-- 1. 数据库创建
-- ================================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `todotask` 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `todotask`;

-- ================================================================
-- 2. 表结构创建
-- ================================================================

-- 知识内容表
-- 存储从RSS源、网页等采集的知识内容信息
CREATE TABLE IF NOT EXISTS `knowledge_content` (
    `know_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '知识内容主键ID，自增长',
    `title` VARCHAR(255) NOT NULL COMMENT '内容标题，不能为空',
    `content` TEXT COMMENT '内容正文，支持大文本存储',
    `source_url` VARCHAR(255) NOT NULL COMMENT '来源URL，不能为空',
    `content_type` VARCHAR(255) NOT NULL COMMENT '内容类型（RSS/Web/Manual等）',
    `acquisition_time` DATETIME(6) NOT NULL COMMENT '采集时间，精确到微秒',
    `tags` VARCHAR(255) DEFAULT NULL COMMENT '标签信息，逗号分隔',
    `processed` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '处理状态，是否经过AI处理',
    `success` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '采集成功状态',
    `error_message` VARCHAR(255) DEFAULT NULL COMMENT '错误信息，采集失败时记录',
    PRIMARY KEY (`know_id`),
    INDEX `idx_content_type` (`content_type`),
    INDEX `idx_processed` (`processed`),
    INDEX `idx_success` (`success`),
    INDEX `idx_acquisition_time` (`acquisition_time`),
    INDEX `idx_source_url` (`source_url`),
    INDEX `idx_title` (`title`(100))
) ENGINE=InnoDB 
DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_unicode_ci 
COMMENT='知识内容表，存储采集的知识内容信息';

-- ================================================================
-- 3. 索引优化
-- ================================================================

-- 复合索引：内容类型 + 处理状态
CREATE INDEX `idx_content_type_processed` ON `knowledge_content` (`content_type`, `processed`);

-- 复合索引：采集时间 + 成功状态
CREATE INDEX `idx_acquisition_time_success` ON `knowledge_content` (`acquisition_time`, `success`);

-- 复合索引：处理状态 + 成功状态
CREATE INDEX `idx_processed_success` ON `knowledge_content` (`processed`, `success`);

-- 全文索引：标题和内容（用于搜索）
-- 注意：MySQL 8.0+ 支持中文全文索引
ALTER TABLE `knowledge_content` ADD FULLTEXT(`title`, `content`) WITH PARSER ngram;

-- ================================================================
-- 4. 初始数据插入
-- ================================================================

-- 插入示例数据
INSERT INTO `knowledge_content` (
    `title`, 
    `content`, 
    `source_url`, 
    `content_type`, 
    `acquisition_time`, 
    `tags`, 
    `processed`, 
    `success`, 
    `error_message`
) VALUES 
(
    'Spring Boot 3.0 新特性详解',
    'Spring Boot 3.0 带来了许多令人兴奋的新特性，包括对Java 17的支持、原生镜像支持、改进的监控功能等。本文将详细介绍这些新特性，帮助开发者更好地使用Spring Boot 3.0。',
    'https://example.com/spring-boot-3-features',
    'RSS',
    NOW(),
    'Spring Boot,Java,框架,技术',
    TRUE,
    TRUE,
    NULL
),
(
    '微服务架构设计最佳实践',
    '微服务架构已经成为现代应用开发的主流模式。本文介绍了微服务架构的设计原则、最佳实践和常见陷阱，帮助开发团队构建可扩展、可维护的微服务系统。',
    'https://example.com/microservices-best-practices',
    'Web',
    NOW(),
    '微服务,架构,设计,最佳实践',
    TRUE,
    TRUE,
    NULL
),
(
    '人工智能在软件开发中的应用',
    '人工智能技术正在改变软件开发的方式。从代码生成到自动化测试，AI为开发者提供了强大的工具。本文探讨了AI在软件开发中的各种应用场景和未来发展趋势。',
    'https://example.com/ai-in-software-development',
    'RSS',
    NOW(),
    '人工智能,软件开发,AI,技术趋势',
    FALSE,
    TRUE,
    NULL
),
(
    '数据库性能优化技巧',
    '数据库性能是应用性能的关键因素。本文介绍了数据库性能优化的各种技巧，包括索引优化、查询优化、连接池配置等，帮助开发者提升数据库性能。',
    'https://example.com/database-performance-optimization',
    'Web',
    NOW(),
    '数据库,性能优化,SQL,索引',
    FALSE,
    TRUE,
    NULL
),
(
    '容器化部署实践指南',
    'Docker和Kubernetes已经成为现代应用部署的标准。本文介绍了容器化部署的完整流程，包括镜像构建、容器编排、服务发现等，帮助开发团队实现高效的容器化部署。',
    'https://example.com/containerization-deployment',
    'RSS',
    NOW(),
    'Docker,Kubernetes,容器化,部署',
    TRUE,
    TRUE,
    NULL
);

-- ================================================================
-- 5. 视图创建
-- ================================================================

-- 统计视图：按内容类型统计
CREATE VIEW `v_content_type_stats` AS
SELECT 
    `content_type`,
    COUNT(*) as `total_count`,
    SUM(CASE WHEN `processed` = TRUE THEN 1 ELSE 0 END) as `processed_count`,
    SUM(CASE WHEN `success` = TRUE THEN 1 ELSE 0 END) as `success_count`,
    AVG(CASE WHEN `success` = TRUE THEN 1 ELSE 0 END) as `success_rate`
FROM `knowledge_content`
GROUP BY `content_type`;

-- 统计视图：按日期统计
CREATE VIEW `v_daily_stats` AS
SELECT 
    DATE(`acquisition_time`) as `acquisition_date`,
    COUNT(*) as `daily_count`,
    SUM(CASE WHEN `processed` = TRUE THEN 1 ELSE 0 END) as `processed_count`,
    SUM(CASE WHEN `success` = TRUE THEN 1 ELSE 0 END) as `success_count`
FROM `knowledge_content`
GROUP BY DATE(`acquisition_time`)
ORDER BY `acquisition_date` DESC;

-- 统计视图：标签统计
CREATE VIEW `v_tag_stats` AS
SELECT 
    TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(`tags`, ',', numbers.n), ',', -1)) as `tag`,
    COUNT(*) as `tag_count`
FROM `knowledge_content`
CROSS JOIN (
    SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) numbers
WHERE CHAR_LENGTH(`tags`) - CHAR_LENGTH(REPLACE(`tags`, ',', '')) >= numbers.n - 1
AND `tags` IS NOT NULL AND `tags` != ''
GROUP BY `tag`
ORDER BY `tag_count` DESC;

-- ================================================================
-- 6. 存储过程创建
-- ================================================================

-- 存储过程：清理过期数据
DELIMITER //
CREATE PROCEDURE `sp_cleanup_old_data`(IN `days_to_keep` INT)
BEGIN
    DECLARE `deleted_count` INT DEFAULT 0;
    
    -- 删除指定天数前的失败记录
    DELETE FROM `knowledge_content` 
    WHERE `success` = FALSE 
    AND `acquisition_time` < DATE_SUB(NOW(), INTERVAL `days_to_keep` DAY);
    
    SET `deleted_count` = ROW_COUNT();
    
    -- 返回删除的记录数
    SELECT `deleted_count` as `deleted_records`;
END //
DELIMITER ;

-- 存储过程：获取内容统计信息
DELIMITER //
CREATE PROCEDURE `sp_get_content_statistics`()
BEGIN
    SELECT 
        COUNT(*) as `total_count`,
        SUM(CASE WHEN `processed` = TRUE THEN 1 ELSE 0 END) as `processed_count`,
        SUM(CASE WHEN `success` = TRUE THEN 1 ELSE 0 END) as `success_count`,
        COUNT(DISTINCT `content_type`) as `content_type_count`,
        COUNT(DISTINCT DATE(`acquisition_time`)) as `active_days`
    FROM `knowledge_content`;
END //
DELIMITER ;

-- ================================================================
-- 7. 触发器创建
-- ================================================================

-- 触发器：更新采集时间（如果为空）
DELIMITER //
CREATE TRIGGER `tr_knowledge_content_before_insert`
BEFORE INSERT ON `knowledge_content`
FOR EACH ROW
BEGIN
    -- 如果采集时间为空，设置为当前时间
    IF NEW.`acquisition_time` IS NULL THEN
        SET NEW.`acquisition_time` = NOW();
    END IF;
END //
DELIMITER ;

-- ================================================================
-- 8. 权限设置
-- ================================================================

-- 创建应用用户（生产环境建议）
-- CREATE USER 'todotask_app'@'localhost' IDENTIFIED BY 'secure_password';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON `todotask`.* TO 'todotask_app'@'localhost';
-- FLUSH PRIVILEGES;

-- ================================================================
-- 9. 数据完整性检查
-- ================================================================

-- 检查表结构
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    DATA_LENGTH,
    INDEX_LENGTH,
    CREATE_TIME
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'todotask' 
AND TABLE_NAME = 'knowledge_content';

-- 检查索引
SELECT 
    INDEX_NAME,
    COLUMN_NAME,
    NON_UNIQUE,
    INDEX_TYPE
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = 'todotask' 
AND TABLE_NAME = 'knowledge_content'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

-- ================================================================
-- 10. 性能优化建议
-- ================================================================

-- 分析表统计信息
ANALYZE TABLE `knowledge_content`;

-- 优化表结构
OPTIMIZE TABLE `knowledge_content`;

-- ================================================================
-- 脚本执行完成
-- ================================================================

-- 显示执行结果
SELECT 'Database script executed successfully!' as `status`;
SELECT COUNT(*) as `total_records` FROM `knowledge_content`;
SELECT * FROM `v_content_type_stats`;
