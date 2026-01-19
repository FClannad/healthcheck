-- AI咨询记录表
-- 请在 MySQL 数据库 fengbinbin 中执行此脚本

CREATE TABLE IF NOT EXISTS `ai_consultation` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  `user_question` text COLLATE utf8mb4_unicode_ci COMMENT '用户问题',
  `ai_response` text COLLATE utf8mb4_unicode_ci COMMENT 'AI回复',
  `recommended_exams` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '推荐的体检项目',
  `session_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '会话ID',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'active' COMMENT '状态：active, archived, deleted',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI咨询记录表';
