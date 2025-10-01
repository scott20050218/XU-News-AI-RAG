-- 更新knowledge_content表的content字段长度
-- 从VARCHAR(10000)增加到VARCHAR(50000)

ALTER TABLE knowledge_content MODIFY COLUMN content TEXT;
