-- AI智能出题引擎所需表结构

-- 知识点表
CREATE TABLE IF NOT EXISTS `knowledge_point` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `subject_id` BIGINT NOT NULL COMMENT '学科ID',
  `grade_id` BIGINT NOT NULL COMMENT '年级ID',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父知识点ID',
  `name` VARCHAR(100) NOT NULL COMMENT '知识点名称',
  `description` TEXT COMMENT '知识点描述',
  `level` INT DEFAULT 1 COMMENT '层级',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_subject_grade` (`subject_id`, `grade_id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识点表';

-- 学习记录表（艾宾浩斯记忆曲线）
CREATE TABLE IF NOT EXISTS `study_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id` BIGINT NOT NULL COMMENT '学生ID',
  `question_id` BIGINT NOT NULL COMMENT '题目ID',
  `knowledge_point_id` BIGINT COMMENT '知识点ID',
  `result` TINYINT NOT NULL COMMENT '答题结果 0:错误 1:正确',
  `time_spent` INT DEFAULT 0 COMMENT '答题耗时(秒)',
  `studied_at` DATETIME NOT NULL COMMENT '学习时间',
  `next_review_at` DATETIME COMMENT '下次复习时间',
  `review_count` INT DEFAULT 0 COMMENT '复习次数',
  `ebbinghaus_step` VARCHAR(20) DEFAULT 'initial' COMMENT '艾宾浩斯阶段',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_student` (`student_id`),
  KEY `idx_question` (`question_id`),
  KEY `idx_knowledge_point` (`knowledge_point_id`),
  KEY `idx_next_review` (`next_review_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习记录表';

-- AI生成记录表
CREATE TABLE IF NOT EXISTS `ai_question_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` BIGINT COMMENT '租户ID',
  `student_id` BIGINT COMMENT '学生ID',
  `question_ids` TEXT COMMENT '生成的题目ID列表',
  `question_type` VARCHAR(20) COMMENT '出题类型 generate/transform/ebbinghaus',
  `transform_type` VARCHAR(20) COMMENT '变形类型',
  `base_question_id` BIGINT COMMENT '基题ID',
  `knowledge_point_ids` VARCHAR(500) COMMENT '知识点IDs',
  `count` INT DEFAULT 0 COMMENT '生成数量',
  `ai_provider` VARCHAR(20) COMMENT 'AI提供商',
  `ai_model` VARCHAR(50) COMMENT 'AI模型',
  `request_prompt` TEXT COMMENT '请求提示词',
  `response_content` TEXT COMMENT 'AI响应内容',
  `status` TINYINT DEFAULT 1 COMMENT '状态',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant` (`tenant_id`),
  KEY `idx_student` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI生成记录表';

-- 为现有question表添加source字段
ALTER TABLE `question` ADD COLUMN `source` VARCHAR(50) DEFAULT NULL COMMENT '题目来源 如:AI生成/AI变形/手动录入' AFTER `year`;

-- 创建知识点时示例数据
INSERT INTO `knowledge_point` (`subject_id`, `grade_id`, `name`, `level`, `sort_order`) VALUES
(1, 1, '有理数', 1, 1),
(1, 1, '整式', 1, 2),
(1, 1, '一元一次方程', 1, 3),
(1, 2, '二次根式', 1, 1),
(1, 2, '一元二次方程', 1, 2);
