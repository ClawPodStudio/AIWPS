# Milestone 2: AI生成变形题 + 知识点掌握追踪

## 概述

本里程碑实现：
1. AI生成变形题 - 根据错题生成同知识点的新题
2. 知识点掌握标记与追踪
3. 艾宾浩斯记忆曲线复习

## 数据库设计

### 1. 变形题记录表 (variant_question)

```sql
CREATE TABLE variant_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    original_question_id BIGINT NOT NULL COMMENT '原题ID',
    variant_question_id BIGINT NOT NULL COMMENT '变形题ID',
    student_id BIGINT NOT NULL COMMENT '生成给哪个学生',
    variant_type VARCHAR(50) COMMENT '变形类型：NUMBER_CHANGE/PHRASE_CHANGE/CONTEXT_CHANGE',
    knowledge_point_id BIGINT COMMENT '关联知识点',
    generated_by VARCHAR(20) DEFAULT 'AI' COMMENT 'AI/MANUAL',
    status TINYINT DEFAULT 0 COMMENT '0待审核 1已通过 2已使用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_original (original_question_id),
    INDEX idx_student (student_id)
) COMMENT '变形题记录';
```

### 2. 知识点掌握表变更 (knowledge_mastery)

```sql
-- 新增字段
ALTER TABLE knowledge_mastery 
ADD COLUMN mastery_level DECIMAL(5,2) DEFAULT 0 COMMENT '掌握度0-100',
ADD COLUMN correct_count INT DEFAULT 0 COMMENT '正确次数',
ADD COLUMN wrong_count INT DEFAULT 0 COMMENT '错误次数',
ADD COLUMN last_review_time DATETIME COMMENT '上次复习时间',
ADD COLUMN next_review_time DATETIME COMMENT '下次复习时间(艾宾浩斯)',
ADD COLUMN is_removed TINYINT DEFAULT 0 COMMENT '是否从出题范围移除';
```

## API设计

### AI变形题生成

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/ai/generate-variant` | POST | 生成变形题 |
| `/api/ai/generate-variant-batch` | POST | 批量生成变形题 |
| `/api/variant-question/list` | GET | 获取变形题列表 |
| `/api/variant-question/{id}/approve` | POST | 审核通过变形题 |

### 知识点掌握

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/knowledge-mastery/detail` | GET | 获取知识点掌握详情 |
| `/api/knowledge-mastery/update` | POST | 更新掌握状态(老师标记) |
| `/api/knowledge-mastery/toggle-remove` | POST | 启用/禁用知识点 |
| `/api/knowledge-mastery/stats` | GET | 知识点统计 |

### 艾宾浩斯复习

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/review/ebbinghaus/today` | GET | 获取今日复习任务 |
| `/api/review/calculate` | POST | 计算下次复习时间 |

## AI生成算法

### 变形类型
1. **NUMBER_CHANGE**: 数值改变（如 10 → 20）
2. **PHRASE_CHANGE**: 问法改变（"求速度" → "求加速度"）
3. **CONTEXT_CHANGE**: 情境改变（汽车 → 火车）
4. **OPTION_SHUFFLE**: 选项打乱

### 艾宾浩斯记忆周期
```
间隔 = [1, 2, 4, 7, 15, 30, 60] 天
因子 = Math.pow(0.9, 错误次数)
实际间隔 = 基础间隔 × 因子
```

## 前端页面

1. **学生端 - 变形题练习**: /student/variant-practice
2. **老师端 - AI出题**: /teacher/ai-generate
3. **老师端 - 知识点掌握**: /teacher/mastery-management

---

*创建日期: 2026-03-25*
