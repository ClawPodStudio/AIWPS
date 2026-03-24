-- AIWPS Database Schema
-- 初高中个性化辅导SaaS系统数据库表

CREATE DATABASE IF NOT EXISTS aiwps DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE aiwps;

-- 租户表
CREATE TABLE IF NOT EXISTS tenant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '租户ID',
    name VARCHAR(100) NOT NULL COMMENT '租户名称',
    province VARCHAR(50) COMMENT '覆盖省份',
    grade_range VARCHAR(50) COMMENT '学段范围',
    subject_range VARCHAR(200) COMMENT '学科范围',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '租户表';

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL COMMENT 'STUDENT/TEACHER/ADMIN',
    name VARCHAR(50) NOT NULL,
    mobile VARCHAR(20),
    grade_id BIGINT COMMENT '学生年级',
    subject_ids VARCHAR(200) COMMENT '授课老师学科ID',
    org_id BIGINT COMMENT '所属机构/学校ID',
    class_id BIGINT COMMENT '班级ID',
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_role (role)
) COMMENT '用户表';

-- 学科表
CREATE TABLE IF NOT EXISTS subject (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '学科名称',
    code VARCHAR(20) NOT NULL UNIQUE COMMENT '学科代码',
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '学科表';

-- 年级表
CREATE TABLE IF NOT EXISTS grade (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(20) NOT NULL COMMENT '年级名称',
    level VARCHAR(10) NOT NULL COMMENT '初中/高中',
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '年级表';

-- 知识点表
CREATE TABLE IF NOT EXISTS knowledge_point (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    subject_id BIGINT NOT NULL,
    grade_id BIGINT NOT NULL,
    parent_id BIGINT DEFAULT 0 COMMENT '父级知识点ID',
    name VARCHAR(100) NOT NULL COMMENT '知识点名称',
    level INT DEFAULT 1 COMMENT '层级 1/2/3',
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_subject (subject_id),
    INDEX idx_grade (grade_id)
) COMMENT '知识点表';

-- 题目表
CREATE TABLE IF NOT EXISTS question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    grade_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT '题型: CHOICE/BLANK/ANSWER/IMAGE/EXPERIMENT/COMPOSITION',
    content TEXT NOT NULL COMMENT '题目内容',
    option_a VARCHAR(500),
    option_b VARCHAR(500),
    option_c VARCHAR(500),
    option_d VARCHAR(500),
    answer TEXT NOT NULL COMMENT '正确答案',
    analysis TEXT COMMENT '解析',
    difficulty INT DEFAULT 2 COMMENT '难度 1易 2中 3难',
    province VARCHAR(50),
    year INT,
    source VARCHAR(100) COMMENT '来源',
    knowledge_point_ids VARCHAR(200) COMMENT '关联知识点ID',
    tags VARCHAR(200) COMMENT '标签',
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_subject (subject_id),
    INDEX idx_grade (grade_id)
) COMMENT '题目表';

-- 错题表
CREATE TABLE IF NOT EXISTS wrong_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    wrong_answer TEXT,
    is_corrected TINYINT DEFAULT 0 COMMENT '是否已纠正',
    wrong_count INT DEFAULT 1,
    last_wrong_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 1 COMMENT '1未掌握 2已掌握',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_student (student_id),
    INDEX idx_question (question_id)
) COMMENT '错题表';

-- 学习记录表
CREATE TABLE IF NOT EXISTS study_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    result TINYINT COMMENT '1正确 0错误',
    time_spent INT COMMENT '耗时(秒)',
    source VARCHAR(50) COMMENT '来源: WRONG/TASK/PRACTICE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_student (student_id),
    INDEX idx_question (question_id)
) COMMENT '学习记录表';

-- 知识点掌握度表
CREATE TABLE IF NOT EXISTS knowledge_mastery (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    knowledge_point_id BIGINT NOT NULL,
    mastery_level INT DEFAULT 0 COMMENT '0未掌握 1部分掌握 2牢固掌握',
    correct_count INT DEFAULT 0,
    total_count INT DEFAULT 0,
    last_review_time DATETIME,
    mastery_status TINYINT DEFAULT 1 COMMENT '1巩固中 2已移除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_kp (student_id, knowledge_point_id),
    INDEX idx_student (student_id)
) COMMENT '知识点掌握度表';

-- 班级表
CREATE TABLE IF NOT EXISTS class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    org_id BIGINT NOT NULL,
    grade_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    teacher_id BIGINT COMMENT '班主任',
    student_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_org (org_id)
) COMMENT '班级表';

-- 机构/学校表
CREATE TABLE IF NOT EXISTS organization (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) COMMENT 'SCHOOL/INSTITUTE',
    province VARCHAR(50),
    contact_name VARCHAR(50),
    contact_mobile VARCHAR(20),
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id)
) COMMENT '机构/学校表';

-- 艾宾浩斯复习计划表
CREATE TABLE IF NOT EXISTS ebbinghaus_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    knowledge_point_id BIGINT NOT NULL,
    review_round INT DEFAULT 1 COMMENT '复习轮次',
    next_review_time DATETIME NOT NULL COMMENT '下次复习时间',
    interval_days INT DEFAULT 1 COMMENT '间隔天数',
    status TINYINT DEFAULT 1 COMMENT '1待复习 2已完成',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_student (student_id),
    INDEX idx_next_review (next_review_time)
) COMMENT '艾宾浩斯复习计划表';

-- 试卷表
CREATE TABLE IF NOT EXISTS paper (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    subject_id BIGINT NOT NULL,
    grade_id BIGINT,
    total_score INT DEFAULT 100,
    question_count INT DEFAULT 0,
    paper_type VARCHAR(20) COMMENT 'GENERAL/WRONG/KNOWLEDGE',
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_teacher (teacher_id)
) COMMENT '试卷表';

-- 试卷题目关联表
CREATE TABLE IF NOT EXISTS paper_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    paper_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    score INT DEFAULT 0,
    sort_order INT DEFAULT 0,
    INDEX idx_paper (paper_id)
) COMMENT '试卷题目关联表';

-- 初始化基础数据
INSERT INTO subject (name, code, sort_order) VALUES 
('语文', 'CHINESE', 1),
('数学', 'MATH', 2),
('英语', 'ENGLISH', 3),
('物理', 'PHYSICS', 4),
('化学', 'CHEMISTRY', 5),
('生物', 'BIOLOGY', 6),
('历史', 'HISTORY', 7),
('地理', 'GEOGRAPHY', 8),
('政治', 'POLITICS', 9);

INSERT INTO grade (name, level, sort_order) VALUES
('初一', '初中', 1),
('初二', '初中', 2),
('初三', '初中', 3),
('高一', '高中', 4),
('高二', '高中', 5),
('高三', '高中', 6);
