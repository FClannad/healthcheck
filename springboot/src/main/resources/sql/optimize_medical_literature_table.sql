-- 医疗文献表优化脚本
-- 安全删除不必要的字段，保留现有数据
-- 执行前请确保已备份数据库

USE xm_health_check;

-- 1. 创建备份表（可选，建议执行）
-- CREATE TABLE medical_literature_backup AS
-- SELECT * FROM medical_literature;

-- 2. 显示当前表结构（用于确认）
DESCRIBE medical_literature;

-- 3. 检查当前数据量
SELECT COUNT(*) as total_records FROM medical_literature;

-- 4. 安全删除不必要的字段
-- 注意：删除字段是不可逆操作，请确保已备份

-- 删除PDF相关字段
ALTER TABLE medical_literature DROP COLUMN pdf_url;

-- 删除DOI字段（很少使用）
ALTER TABLE medical_literature DROP COLUMN doi;

-- 删除统计字段（非核心功能）
ALTER TABLE medical_literature DROP COLUMN view_count;
ALTER TABLE medical_literature DROP COLUMN download_count;

-- 删除更新时间字段（冗余）
ALTER TABLE medical_literature DROP COLUMN update_time;

-- 删除语言字段（很少使用）
ALTER TABLE medical_literature DROP COLUMN language;

-- 删除影响因子字段（很少使用）
ALTER TABLE medical_literature DROP COLUMN impact;

-- 删除标签字段（与keywords重复）
ALTER TABLE medical_literature DROP COLUMN tags;

-- 删除分类字段（可以从keywords推导）
ALTER TABLE medical_literature DROP COLUMN category;

-- 5. 优化字段长度和类型
-- 扩展title字段长度以支持长标题
ALTER TABLE medical_literature 
MODIFY COLUMN title VARCHAR(1000) NOT NULL COMMENT '文献标题';

-- 扩展authors字段长度
ALTER TABLE medical_literature 
MODIFY COLUMN authors VARCHAR(1000) COMMENT '作者列表';

-- 扩展journal字段长度
ALTER TABLE medical_literature 
MODIFY COLUMN journal VARCHAR(300) COMMENT '期刊名称';

-- 扩展keywords字段长度
ALTER TABLE medical_literature 
MODIFY COLUMN keywords VARCHAR(800) COMMENT '关键词';

-- 扩展source_url字段长度
ALTER TABLE medical_literature 
MODIFY COLUMN source_url VARCHAR(1000) COMMENT '原文链接';

-- 确保crawl_source不为空
ALTER TABLE medical_literature 
MODIFY COLUMN crawl_source VARCHAR(50) NOT NULL COMMENT '爬取来源';

-- 6. 添加/优化索引
-- 删除可能存在的旧索引（忽略错误）
-- DROP INDEX idx_category ON medical_literature;
-- DROP INDEX idx_view_count ON medical_literature;
-- DROP INDEX idx_doi ON medical_literature;

-- 添加新的优化索引
CREATE INDEX idx_crawl_source ON medical_literature (crawl_source);
CREATE INDEX idx_keywords ON medical_literature (keywords(100));

-- 7. 清理数据
-- 确保status字段有默认值
UPDATE medical_literature 
SET status = 'active' 
WHERE status IS NULL OR status = '';

-- 确保crawl_source字段不为空
UPDATE medical_literature 
SET crawl_source = 'unknown' 
WHERE crawl_source IS NULL OR crawl_source = '';

-- 8. 显示优化后的表结构
DESCRIBE medical_literature;

-- 9. 验证数据完整性
SELECT 
    COUNT(*) as total_records,
    COUNT(DISTINCT crawl_source) as unique_sources,
    COUNT(CASE WHEN status = 'active' THEN 1 END) as active_records
FROM medical_literature;

-- 10. 显示各数据源的文献数量
SELECT crawl_source, COUNT(*) as count 
FROM medical_literature 
GROUP BY crawl_source 
ORDER BY count DESC;

COMMIT;

-- 优化完成提示
SELECT 'Medical literature table optimization completed successfully!' as message;
