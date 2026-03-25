# Milestone 3: AI生成试卷

## 概述

实现AI智能组卷功能，支持根据知识点、题型、难度自动生成试卷。

## 数据库设计

### 试卷模板表 (paper_template)

```sql
CREATE TABLE paper_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    subject_id BIGINT NOT NULL COMMENT '学科ID',
    grade_id BIGINT COMMENT '年级ID',
    question_type_config JSON NOT NULL COMMENT '{"CHOICE":10,"BLANK":5,"DRAWING":2}',
    difficulty_config JSON COMMENT '{"easy":0.3,"medium":0.5,"hard":0.2}',
    total_score INT DEFAULT 100 COMMENT '总分',
    time_limit INT COMMENT '时间限制(分钟)',
    created_by BIGINT COMMENT '创建人',
    status TINYINT DEFAULT 1 COMMENT '1启用 0禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_subject (subject_id),
    INDEX idx_tenant (tenant_id)
) COMMENT '试卷模板';
```

### 试卷表变更 (paper)

```sql
ALTER TABLE paper
ADD COLUMN is_ai_generated TINYINT DEFAULT 0 COMMENT '是否AI生成',
ADD COLUMN knowledge_point_ids JSON COMMENT '关联知识点ID列表',
ADD COLUMN generated_by VARCHAR(20) DEFAULT 'MANUAL' COMMENT 'AI/MANUAL',
ADD COLUMN review_status TINYINT DEFAULT 0 COMMENT '0待审核 1已通过 2已使用';
```

## API设计

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/paper/ai-generate` | POST | AI生成试卷 |
| `/api/paper/templates` | GET | 获取模板列表 |
| `/api/paper/template/{id}` | GET | 获取模板详情 |
| `/api/paper/template` | POST | 创建模板 |
| `/api/paper/knowledge-range` | GET | 获取可选知识点范围 |
| `/api/paper/review/{id}` | POST | 审核试卷 |

### AI生成请求格式

```json
{
  "subjectId": 1,
  "gradeId": 1,
  "knowledgePointIds": [1, 2, 3],
  "questionTypes": {"CHOICE": 5, "BLANK": 3, "CALCULATION": 2},
  "difficulty": {"easy": 0.3, "medium": 0.5, "hard": 0.2},
  "totalScore": 100
}
```

## AI组卷算法

1. **知识点分布**：按知识点在历年考试中出现频次分配题目数量
2. **难度控制**：根据配置的难度比例，在每个知识点的题目库中筛选
3. **去重**：避免同一题目在同一试卷中出现多次
4. **总分校验**：确保生成试卷总分等于目标总分

## 前端页面

1. **试卷生成页**: /teacher/paper-generate
2. **模板管理页**: /teacher/paper-templates
3. **试卷审核页**: /teacher/paper-review

---

*创建日期: 2026-03-25*
