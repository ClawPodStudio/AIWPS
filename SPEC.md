# AIWPS - 初高中个性化辅导SaaS系统技术规格

## 1. 系统架构

### 1.1 技术栈
- **后端**: Spring Boot 3.x + MyBatis-Plus
- **前端**: Vue 3 + Element Plus
- **数据库**: MySQL 8.0
- **AI**: 对接大模型API实现智能出题

### 1.2 模块划分
1. **aiwps-admin** - 基础管理模块（租户管理、权限管理）
2. **aiwps-question** - 题库管理模块
3. **aiwps-student** - 学生端模块
4. **aiwps-teacher** - 教师端模块
5. **aiwps-org** - 机构/学校端模块
6. **aiwps-common** - 公共模块

### 1.3 核心功能
- 多租户隔离
- 题库管理（知识点拆分、标签化）
- 错题管理
- AI智能出题
- 艾宾浩斯记忆曲线

## 2. 数据库设计

### 2.1 租户表 (tenant)
- id, name, province, grade_range, subject_range, status, created_at

### 2.2 用户表 (user)
- id, tenant_id, username, password, role, name, mobile, status

### 2.3 学科表 (subject)
- id, name, code

### 2.4 年级表 (grade)
- id, name, level (初中/高中), sort_order

### 2.5 知识点表 (knowledge_point)
- id, subject_id, grade_id, parent_id, name, level, sort_order

### 2.6 题目表 (question)
- id, tenant_id, subject_id, grade_id, type, content, answer, analysis, difficulty, province, year, knowledge_points, tags, status

### 2.7 错题表 (wrong_question)
- id, student_id, question_id, wrong_answer, correct_answer, wrong_time, status

### 2.8 学习记录表 (study_record)
- id, student_id, question_id, result, time_spent, created_at
