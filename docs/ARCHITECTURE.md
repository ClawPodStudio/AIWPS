# AIWPS 初高中个性化辅导SaaS系统 - 架构设计文档

## 一、系统整体架构

### 1.1 架构设计原则

- **多租户隔离**: 采用数据库级租户隔离，每个租户数据完全独立
- **微服务化**: 按业务模块拆分，便于扩展和维护
- **前后端分离**: Spring Boot + Vue 3 技术栈
- **AI能力接入**: 预留AI接口，支持智能出题和个性化推荐

### 1.2 系统架构图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           AIWPS SaaS 系统架构                             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐               │
│  │   学生端    │     │   教师端    │     │   管理端    │               │
│  │  Web/App   │     │  Web/App   │     │  Web/App   │               │
│  └──────┬──────┘     └──────┬──────┘     └──────┬──────┘               │
│         │                  │                  │                         │
│         └──────────────────┼──────────────────┘                         │
│                            │                                            │
│                    ┌───────▼───────┐                                    │
│                    │   API网关    │                                    │
│                    │  (Gateway)   │                                    │
│                    └───────┬───────┘                                    │
│                            │                                            │
│         ┌──────────────────┼──────────────────┐                        │
│         │                  │                  │                         │
│  ┌──────▼──────┐    ┌──────▼──────┐    ┌──────▼──────┐                │
│  │   admin    │    │  question   │    │  student    │                │
│  │   管理模块  │    │   题库模块   │    │   学生模块   │                │
│  └──────┬──────┘    └──────┬──────┘    └──────┬──────┘                │
│         │                  │                  │                         │
│  ┌──────▼──────┐    ┌──────▼──────┐    ┌──────▼──────┐                │
│  │  teacher   │    │    org      │    │   common    │                │
│  │   教师模块  │    │   机构模块   │    │   公共模块   │                │
│  └─────────────┘    └─────────────┘    └─────────────┘                │
│                            │                                            │
│                    ┌───────▼───────┐                                    │
│                    │   AI引擎     │                                    │
│                    │ (AI Engine)  │                                    │
│                    └───────────────┘                                    │
│                            │                                            │
│                    ┌───────▼───────┐                                    │
│                    │   MySQL 8.0  │                                    │
│                    │ (多租户DB)   │                                    │
│                    └───────────────┘                                    │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1.3 多租户架构设计

#### 租户隔离策略
- **数据库级隔离**: 每个租户拥有独立的数据库实例（或表空间）
- **Tenant ID强制校验**: 所有业务表通过tenant_id字段关联
- **租户配置**: 支持按省份、学段、学科范围配置

#### 多租户数据流向
```
请求 → API网关 → 租户解析 → 路由到对应租户数据库 → 返回结果
```

### 1.4 模块说明

| 模块 | 功能描述 | 端口 |
|------|---------|------|
| aiwps-admin | 基础管理（租户、权限、用户） | 8081 |
| aiwps-question | 题库管理（题目、知识点、标签） | 8082 |
| aiwps-student | 学生端（学习、错题、练习） | 8083 |
| aiwps-teacher | 教师端（组卷、批改、统计） | 8084 |
| aiwps-org | 机构/学校管理 | 8085 |
| aiwps-common | 公共模块（工具类、配置） | - |

---

## 二、数据库Schema设计

### 2.1 ER图关系

```
┌──────────┐       ┌──────────┐       ┌──────────┐
│  tenant  │◄──────│   user   │◄──────│  class   │
└──────────┘       └────┬─────┘       └────┬─────┘
                        │                │
                        │         ┌──────▼──────┐
                        │         │ organization│
                        │         └──────┬──────┘
                        │                │
              ┌─────────┼─────────┐       │
              │         │         │       │
        ┌─────▼────┐ ┌──▼──┐ ┌───▼────┐ ┌─▼─────┐
        │ subject │ │grade│ │knowledge│ │question│
        └─────────┘ └─────┘ │ point  │ └───────┘
                            └────────┘        │
                                         ┌────▼────┐
                                         │wrong_q │
                                         └────────┘
```

### 2.2 核心表结构

#### 2.2.1 租户表 (tenant)

```sql
CREATE TABLE IF NOT EXISTS tenant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '租户ID',
    name VARCHAR(100) NOT NULL COMMENT '租户名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '租户编码',
    province VARCHAR(200) COMMENT '覆盖省份(JSON数组)',
    grade_range VARCHAR(50) COMMENT '学段范围: 初中/高中/初高中',
    subject_range VARCHAR(200) COMMENT '学科范围(JSON数组)',
    db_name VARCHAR(50) COMMENT '数据库名',
    db_host VARCHAR(100) COMMENT '数据库地址',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    expire_date DATE COMMENT '过期日期',
    max_users INT DEFAULT 100 COMMENT '最大用户数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code (code)
) COMMENT '租户表';
```

#### 2.2.2 用户表 (user)

```sql
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL COMMENT 'STUDENT/TEACHER/ADMIN/ORG_ADMIN',
    name VARCHAR(50) NOT NULL,
    avatar VARCHAR(500) COMMENT '头像URL',
    mobile VARCHAR(20),
    email VARCHAR(100),
    grade_id BIGINT COMMENT '学生年级',
    subject_ids VARCHAR(200) COMMENT '授课老师学科ID,逗号分隔',
    org_id BIGINT COMMENT '所属机构/学校ID',
    class_id BIGINT COMMENT '班级ID',
    status TINYINT DEFAULT 1 COMMENT '0禁用 1正常',
    last_login_time DATETIME,
    last_login_ip VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_username (tenant_id, username),
    INDEX idx_role (role),
    INDEX idx_org (org_id),
    INDEX idx_class (class_id),
    UNIQUE KEY uk_tenant_username (tenant_id, username)
) COMMENT '用户表';
```

#### 2.2.3 机构/学校表 (organization)

```sql
CREATE TABLE IF NOT EXISTS organization (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL COMMENT '机构/学校名称',
    code VARCHAR(50) COMMENT '机构编码',
    type VARCHAR(20) NOT NULL COMMENT 'SCHOOL/INSTITUTE',
    level VARCHAR(20) COMMENT '办学层次: 初中/高中/完中/九年一贯制',
    province VARCHAR(50),
    city VARCHAR(50),
    district VARCHAR(50),
    address VARCHAR(200),
    contact_name VARCHAR(50),
    contact_mobile VARCHAR(20),
    contact_email VARCHAR(100),
    student_count INT DEFAULT 0,
    teacher_count INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_type (type)
) COMMENT '机构/学校表';
```

#### 2.2.4 班级表 (class)

```sql
CREATE TABLE IF NOT EXISTS class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    org_id BIGINT NOT NULL,
    grade_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL COMMENT '班级名称',
    teacher_id BIGINT COMMENT '班主任ID',
    student_count INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_org (org_id),
    INDEX idx_teacher (teacher_id)
) COMMENT '班级表';
```

#### 2.2.5 学科表 (subject)

```sql
CREATE TABLE IF NOT EXISTS subject (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '学科名称',
    code VARCHAR(20) NOT NULL UNIQUE COMMENT '学科代码',
    icon VARCHAR(100) COMMENT '图标',
    color VARCHAR(20) COMMENT '主题色',
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sort (sort_order)
) COMMENT '学科表';
```

#### 2.2.6 年级表 (grade)

```sql
CREATE TABLE IF NOT EXISTS grade (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(20) NOT NULL COMMENT '年级名称',
    code VARCHAR(20) COMMENT '年级代码',
    level VARCHAR(10) NOT NULL COMMENT '初中/高中',
    level_type VARCHAR(10) COMMENT 'JUNIOR/SENIOR',
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_level (level),
    INDEX idx_sort (sort_order)
) COMMENT '年级表';
```

#### 2.2.7 知识点表 (knowledge_point)

```sql
CREATE TABLE IF NOT EXISTS knowledge_point (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    grade_id BIGINT NOT NULL,
    parent_id BIGINT DEFAULT 0 COMMENT '父级知识点ID,0为根节点',
    name VARCHAR(100) NOT NULL COMMENT '知识点名称',
    code VARCHAR(50) COMMENT '知识点编码',
    level INT DEFAULT 1 COMMENT '层级 1/2/3',
    sort_order INT DEFAULT 0,
    weight INT DEFAULT 1 COMMENT '权重(重要程度)',
    description VARCHAR(500) COMMENT '知识点描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_subject (subject_id),
    INDEX idx_grade (grade_id),
    INDEX idx_parent (parent_id)
) COMMENT '知识点表';
```

#### 2.2.8 题目表 (question)

```sql
CREATE TABLE IF NOT EXISTS question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    grade_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT '题型: CHOICE/BLANK/ANSWER/IMAGE/EXPERIMENT/COMPOSITION/CLOZE/READING',
    content TEXT NOT NULL COMMENT '题目内容(JSON格式,含图片等)',
    option_a VARCHAR(1000),
    option_b VARCHAR(1000),
    option_c VARCHAR(1000),
    option_d VARCHAR(1000),
    option_e VARCHAR(1000),
    option_f VARCHAR(1000),
    answer TEXT NOT NULL COMMENT '正确答案(JSON格式)',
    analysis TEXT COMMENT '解析(JSON格式)',
    difficulty INT DEFAULT 2 COMMENT '难度 1简单 2中等 3困难 4竞赛',
    province VARCHAR(50) COMMENT '适用省份',
    year INT COMMENT '年份',
    source VARCHAR(200) COMMENT '来源(中考真题/模拟题/原创)',
    source_type VARCHAR(20) COMMENT 'SOURCE_TYPE: ZK/MN/CREATE',
    knowledge_point_ids VARCHAR(200) COMMENT '关联知识点ID,逗号分隔',
    tags VARCHAR(500) COMMENT '标签,逗号分隔',
    total_attempts INT DEFAULT 0 COMMENT '总作答次数',
    correct_rate DECIMAL(5,2) DEFAULT 0 COMMENT '正确率',
    status TINYINT DEFAULT 1 COMMENT '0待审核 1正常 2禁用',
    creator_id BIGINT COMMENT '创建人ID',
    reviewer_id BIGINT COMMENT '审核人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_subject (subject_id),
    INDEX idx_grade (grade_id),
    INDEX idx_type (type),
    INDEX idx_difficulty (difficulty),
    INDEX idx_knowledge (knowledge_point_ids),
    INDEX idx_status (status),
    FULLTEXT INDEX ft_content (content, analysis)
) COMMENT '题目表';
```

#### 2.2.9 错题表 (wrong_question)

```sql
CREATE TABLE IF NOT EXISTS wrong_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    wrong_answer TEXT COMMENT '错误答案',
    correct_answer TEXT COMMENT '正确答案',
    wrong_count INT DEFAULT 1 COMMENT '错误次数',
    is_corrected TINYINT DEFAULT 0 COMMENT '是否已纠正',
    corrected_at DATETIME COMMENT '纠正时间',
    wrong_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '首次错题时间',
    last_wrong_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后错题时间',
    status TINYINT DEFAULT 1 COMMENT '1未掌握 2已掌握 3已移除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_student (student_id),
    INDEX idx_question (question_id),
    INDEX idx_status (status),
    UNIQUE KEY uk_student_question (tenant_id, student_id, question_id)
) COMMENT '错题表';
```

#### 2.2.10 学习记录表 (study_record)

```sql
CREATE TABLE IF NOT EXISTS study_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    result TINYINT COMMENT '1正确 0错误',
    score INT COMMENT '得分',
    answer TEXT COMMENT '学生答案',
    time_spent INT COMMENT '耗时(秒)',
    source VARCHAR(50) COMMENT '来源: WRONG/TASK/PRACTICE/EXAM/AI',
    source_id BIGINT COMMENT '来源ID(如试卷ID/任务ID)',
    ip_address VARCHAR(50),
    device_type VARCHAR(20) COMMENT '设备类型: PC/MOBILE/PAD',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_student (student_id),
    INDEX idx_question (question_id),
    INDEX idx_source (source, source_id),
    INDEX idx_created (created_at)
) COMMENT '学习记录表';
```

#### 2.2.11 知识点掌握度表 (knowledge_mastery)

```sql
CREATE TABLE IF NOT EXISTS knowledge_mastery (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    knowledge_point_id BIGINT NOT NULL,
    mastery_level INT DEFAULT 0 COMMENT '0未学习 1初学 2掌握 3熟练 4精通',
    correct_count INT DEFAULT 0,
    total_count INT DEFAULT 0,
    avg_time_spent INT DEFAULT 0 COMMENT '平均耗时(秒)',
    last_review_time DATETIME,
    next_review_time DATETIME,
    mastery_status TINYINT DEFAULT 1 COMMENT '1学习中 2已掌握 3已移除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_kp (tenant_id, student_id, knowledge_point_id),
    INDEX idx_student (student_id),
    INDEX idx_mastery (mastery_level)
) COMMENT '知识点掌握度表';
```

#### 2.2.12 艾宾浩斯复习计划表 (ebbinghaus_plan)

```sql
CREATE TABLE IF NOT EXISTS ebbinghaus_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    knowledge_point_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    review_round INT DEFAULT 1 COMMENT '复习轮次(1-5)',
    review_count INT DEFAULT 0 COMMENT '已复习次数',
    next_review_time DATETIME NOT NULL COMMENT '下次复习时间',
    interval_days INT DEFAULT 1 COMMENT '间隔天数',
    ease_factor DECIMAL(3,2) DEFAULT 2.5 COMMENT '易度因子',
    status TINYINT DEFAULT 1 COMMENT '1待复习 2已完成 3已跳过',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_student (student_id),
    INDEX idx_next_review (next_review_time),
    INDEX idx_status (status)
) COMMENT '艾宾浩斯复习计划表';
```

#### 2.2.13 试卷表 (paper)

```sql
CREATE TABLE IF NOT EXISTS paper (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL COMMENT '试卷标题',
    subject_id BIGINT NOT NULL,
    grade_id BIGINT,
    total_score INT DEFAULT 100,
    question_count INT DEFAULT 0,
    paper_type VARCHAR(20) DEFAULT 'GENERAL' COMMENT 'GENERAL/WRONG/KNOWLEDGE/PRACTICE/EXAM',
    difficulty_distribution VARCHAR(200) COMMENT '难度分布JSON',
    time_limit INT COMMENT '时限(分钟)',
    description VARCHAR(500),
    status TINYINT DEFAULT 1 COMMENT '0草稿 1已发布 2已禁用',
    published_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_teacher (teacher_id),
    INDEX idx_subject (subject_id),
    INDEX idx_status (status)
) COMMENT '试卷表';
```

#### 2.2.14 试卷题目关联表 (paper_question)

```sql
CREATE TABLE IF NOT EXISTS paper_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    paper_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    score INT DEFAULT 0 COMMENT '分值',
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_paper (paper_id),
    INDEX idx_question (question_id),
    UNIQUE KEY uk_paper_question (tenant_id, paper_id, question_id)
) COMMENT '试卷题目关联表';
```

#### 2.2.15 练习/任务表 (task)

```sql
CREATE TABLE IF NOT EXISTS task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    task_type VARCHAR(20) NOT NULL COMMENT 'PRACTICE/WRONG_REVIEW/KNOWLEDGE/EXAM',
    subject_id BIGINT NOT NULL,
    grade_id BIGINT,
    paper_id BIGINT COMMENT '关联试卷ID',
    target_type VARCHAR(20) COMMENT 'CLASS/INDIVIDUAL',
    target_ids VARCHAR(500) COMMENT '目标用户/班级ID,逗号分隔',
    start_time DATETIME,
    end_time DATETIME,
    time_limit INT COMMENT '时限(分钟)',
    auto_submit TINYINT DEFAULT 1 COMMENT '是否自动提交',
    show_answer TINYINT DEFAULT 1 COMMENT '是否显示答案',
    show_analysis TINYINT DEFAULT 1 COMMENT '是否显示解析',
    status TINYINT DEFAULT 1 COMMENT '0草稿 1已发布 2进行中 3已结束',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_task_type (task_type),
    INDEX idx_target (target_type, target_ids),
    INDEX idx_status (status)
) COMMENT '练习/任务表';
```

#### 2.2.16 任务提交记录表 (task_submission)

```sql
CREATE TABLE IF NOT EXISTS task_submission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    score INT COMMENT '得分',
    correct_count INT DEFAULT 0,
    wrong_count INT DEFAULT 0,
    time_spent INT COMMENT '耗时(秒)',
    submit_time DATETIME,
    ip_address VARCHAR(50),
    status TINYINT DEFAULT 1 COMMENT '1未提交 2已提交 3超时',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_task (task_id),
    INDEX idx_student (student_id),
    UNIQUE KEY uk_task_student (tenant_id, task_id, student_id)
) COMMENT '任务提交记录表';
```

#### 2.2.17 AI出题记录表 (ai_generate_log)

```sql
CREATE TABLE IF NOT EXISTS ai_generate_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    prompt TEXT COMMENT 'AI提示词',
    response_text TEXT COMMENT 'AI响应',
    question_count INT DEFAULT 0 COMMENT '生成题目数',
    subject_id BIGINT,
    grade_id BIGINT,
    knowledge_point_ids VARCHAR(200),
    difficulty VARCHAR(20),
    status TINYINT DEFAULT 1 COMMENT '1成功 2失败',
    error_msg VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_user (user_id),
    INDEX idx_created (created_at)
) COMMENT 'AI出题记录表';
```

---

## 三、API接口规范

### 3.1 接口规范说明

- **基础路径**: `/api/v1`
- **认证方式**: JWT Token
- **数据格式**: JSON
- **编码**: UTF-8

### 3.2 通用响应结构

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1700000000000
}
```

### 3.3 错误码定义

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权/Token失效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |

### 3.4 认证接口

#### 3.4.1 登录
```
POST /api/v1/auth/login
```

**请求参数**:
```json
{
  "username": "string",
  "password": "string",
  "tenantCode": "string"
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token...",
    "expireIn": 7200,
    "user": {
      "id": 1,
      "username": "admin",
      "name": "管理员",
      "role": "ADMIN",
      "avatar": "https://..."
    }
  }
}
```

#### 3.4.2 刷新Token
```
POST /api/v1/auth/refresh
```

### 3.5 租户管理接口

#### 3.5.1 租户列表
```
GET /api/v1/tenants
```

**Query参数**:
- `page`: 页码 (默认1)
- `pageSize`: 每页数量 (默认10)
- `status`: 状态筛选
- `province`: 省份筛选
- `keyword`: 关键字搜索

#### 3.5.2 租户详情
```
GET /api/v1/tenants/{id}
```

#### 3.5.3 创建租户
```
POST /api/v1/tenants
```

#### 3.5.4 更新租户
```
PUT /api/v1/tenants/{id}
```

#### 3.5.5 删除租户
```
DELETE /api/v1/tenants/{id}
```

### 3.6 用户管理接口

#### 3.6.1 用户列表
```
GET /api/v1/users
```

**Query参数**:
- `page`, `pageSize`
- `role`: STUDENT/TEACHER/ADMIN
- `orgId`: 机构ID
- `classId`: 班级ID
- `gradeId`: 年级
- `keyword`: 用户名/姓名/手机

#### 3.6.2 创建用户
```
POST /api/v1/users
```

**请求体**:
```json
{
  "username": "string",
  "password": "string",
  "name": "string",
  "role": "STUDENT",
  "mobile": "string",
  "orgId": 1,
  "classId": 1,
  "gradeId": 1,
  "subjectIds": [1, 2]
}
```

#### 3.6.3 更新用户
```
PUT /api/v1/users/{id}
```

#### 3.6.4 删除用户
```
DELETE /api/v1/users/{id}
```

#### 3.6.5 重置密码
```
POST /api/v1/users/{id}/reset-password
```

### 3.7 机构管理接口

#### 3.7.1 机构列表
```
GET /api/v1/orgs
```

#### 3.7.2 创建机构
```
POST /api/v1/orgs
```

### 3.8 班级管理接口

#### 3.8.1 班级列表
```
GET /api/v1/classes
```

#### 3.8.2 创建班级
```
POST /api/v1/classes
```

#### 3.8.3 班级学生列表
```
GET /api/v1/classes/{id}/students
```

### 3.9 题库管理接口

#### 3.9.1 题目列表
```
GET /api/v1/questions
```

**Query参数**:
- `page`, `pageSize`
- `subjectId`: 学科ID
- `gradeId`: 年级ID
- `type`: 题型
- `difficulty`: 难度 (1-4)
- `province`: 省份
- `year`: 年份
- `knowledgePointId`: 知识点ID
- `tag`: 标签
- `status`: 状态
- `keyword`: 关键字搜索

#### 3.9.2 题目详情
```
GET /api/v1/questions/{id}
```

#### 3.9.3 创建题目
```
POST /api/v1/questions
```

**请求体**:
```json
{
  "subjectId": 1,
  "gradeId": 1,
  "type": "CHOICE",
  "content": "题目内容",
  "optionA": "A选项",
  "optionB": "B选项",
  "optionC": "C选项",
  "optionD": "D选项",
  "answer": "A",
  "analysis": "解析内容",
  "difficulty": 2,
  "province": "北京",
  "year": 2024,
  "source": "中考真题",
  "knowledgePointIds": [1, 2],
  "tags": ["基础", "高频"]
}
```

#### 3.9.4 批量创建题目
```
POST /api/v1/questions/batch
```

#### 3.9.5 更新题目
```
PUT /api/v1/questions/{id}
```

#### 3.9.6 删除题目
```
DELETE /api/v1/questions/{id}
```

#### 3.9.7 题目审核
```
POST /api/v1/questions/{id}/review
```

### 3.10 知识点管理接口

#### 3.10.1 知识点树
```
GET /api/v1/knowledge-points/tree
```

**Query参数**: `subjectId`, `gradeId`

#### 3.10.2 知识点列表
```
GET /api/v1/knowledge-points
```

#### 3.10.3 创建知识点
```
POST /api/v1/knowledge-points
```

#### 3.10.4 更新知识点
```
PUT /api/v1/knowledge-points/{id}
```

#### 3.10.5 删除知识点
```
DELETE /api/v1/knowledge-points/{id}
```

### 3.11 试卷管理接口

#### 3.11.1 试卷列表
```
GET /api/v1/papers
```

#### 3.11.2 试卷详情(含题目)
```
GET /api/v1/papers/{id}
```

#### 3.11.3 创建试卷
```
POST /api/v1/papers
```

#### 3.11.4 添加题目到试卷
```
POST /api/v1/papers/{id}/questions
```

**请求体**:
```json
{
  "questionIds": [1, 2, 3],
  "scores": [5, 5, 10]
}
```

#### 3.11.5 移除试卷题目
```
DELETE /api/v1/papers/{id}/questions/{questionId}
```

#### 3.11.6 发布试卷
```
POST /api/v1/papers/{id}/publish
```

### 3.12 任务/练习接口

#### 3.12.1 任务列表
```
GET /api/v1/tasks
```

#### 3.12.2 创建任务
```
POST /api/v1/tasks
```

#### 3.12.3 发布任务
```
POST /api/v1/tasks/{id}/publish
```

#### 3.12.4 学生任务列表
```
GET /api/v1/student/tasks
```

#### 3.12.5 开始任务
```
POST /api/v1/student/tasks/{id}/start
```

#### 3.12.6 提交答案
```
POST /api/v1/student/tasks/{id}/submit
```

**请求体**:
```json
{
  "answers": [
    {"questionId": 1, "answer": "A"},
    {"questionId": 2, "answer": "B"}
  ],
  "timeSpent": 1800
}
```

### 3.13 错题管理接口

#### 3.13.1 错题列表
```
GET /api/v1/wrong-questions
```

#### 3.13.2 错题详情
```
GET /api/v1/wrong-questions/{id}
```

#### 3.13.3 纠错（重新做）
```
POST /api/v1/wrong-questions/{id}/correct
```

**请求体**:
```json
{
  "answer": "A"
}
```

#### 3.13.4 移除错题
```
DELETE /api/v1/wrong-questions/{id}
```

### 3.14 学习记录接口

#### 3.14.1 练习记录
```
GET /api/v1/study-records
```

#### 3.14.2 统计概览
```
GET /api/v1/student/stats
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "totalQuestions": 1000,
    "correctCount": 800,
    "correctRate": 80,
    "wrongCount": 200,
    "studyTime": 36000,
    "masteryLevel": {
      "knowledgeCount": 50,
      "masteredCount": 30,
      "learningCount": 15,
      "notStartedCount": 5
    }
  }
}
```

### 3.15 AI接口

#### 3.15.1 智能出题
```
POST /api/v1/ai/generate
```

**请求体**:
```json
{
  "subjectId": 1,
  "gradeId": 1,
  "knowledgePointIds": [1, 2],
  "difficulty": "MEDIUM",
  "questionCount": 10,
  "questionType": "CHOICE"
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "questions": [
      {
        "content": "题目内容",
        "optionA": "A",
        "optionB": "B",
        "answer": "A"
      }
    ],
    "taskId": "生成任务ID"
  }
}
```

#### 3.15.2 AI知识诊断
```
POST /api/v1/ai/diagnose
```

#### 3.15.3 个性化推荐
```
GET /api/v1/ai/recommend
```

### 3.16 艾宾浩斯复习接口

#### 3.16.1 获取今日复习计划
```
GET /api/v1/review/today
```

#### 3.16.2 完成复习
```
POST /api/v1/review/{id}/complete
```

---

## 四、附录

### 4.1 数据字典

#### 用户角色
| 角色 | 说明 |
|------|------|
| ADMIN | 系统管理员 |
| ORG_ADMIN | 机构管理员 |
| TEACHER | 教师 |
| STUDENT | 学生 |

#### 题目题型
| 类型 | 说明 |
|------|------|
| CHOICE | 选择题 |
| BLANK | 填空题 |
| ANSWER | 解答题 |
| IMAGE | 图片题 |
| EXPERIMENT | 实验题 |
| COMPOSITION | 作文题 |
| CLOZE | 完形填空 |
| READING | 阅读理解 |

#### 题目难度
| 难度 | 值 | 说明 |
|------|-----|------|
| 简单 | 1 | 基础题 |
| 中等 | 2 | 综合题 |
| 困难 | 3 | 压轴题 |
| 竞赛 | 4 | 竞赛题 |

#### 题目来源
| 来源 | 说明 |
|------|------|
| ZK | 中考真题 |
| MN | 模拟题 |
| CREATE | 原创题 |

#### 任务类型
| 类型 | 说明 |
|------|------|
| PRACTICE | 日常练习 |
| WRONG_REVIEW | 错题复习 |
| KNOWLEDGE | 知识点练习 |
| EXAM | 考试 |

---

## 五、版本记录

| 版本 | 日期 | 说明 | 作者 |
|------|------|------|------|
| v1.0 | 2024-03-24 | 初始版本 | architect |
