# Milestone 1: 知识点题型分类设计

## 概述

本设计文档详细说明知识点题型分类功能的数据库设计、API接口设计和前端页面设计。

**需求背景**：拿物理来说，选择题的知识点、填空题的知识点、画图题的知识点需要分别罗列。

---

## 一、数据库设计

### 1.1 知识点表变更 (knowledge_point)

在现有知识点表中增加以下字段：

```sql
-- 为知识点表增加题型分类支持
ALTER TABLE knowledge_point 
ADD COLUMN question_types JSON COMMENT '题型数组，如["CHOICE","BLANK","DRAWING"]',
ADD COLUMN exam_freq INT DEFAULT 0 COMMENT '历年考试出现频次',
ADD COLUMN score_weight DECIMAL(5,2) DEFAULT 1.0 COMMENT '得分权重';
```

**字段说明**：
| 字段 | 类型 | 说明 |
|------|------|------|
| question_types | JSON | 支持多题型，如 ["CHOICE","BLANK","DRAWING"] |
| exam_freq | INT | 该知识点在历年考试中出现的频次 |
| score_weight | DECIMAL(5,2) | 该知识点的得分权重，默认1.0 |

### 1.2 知识点题型关联表 (knowledge_point_type)

新建知识点与题型的关联表，支持更细粒度的题型管理：

```sql
CREATE TABLE knowledge_point_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    knowledge_point_id BIGINT NOT NULL COMMENT '知识点ID',
    question_type VARCHAR(20) NOT NULL COMMENT '题型：CHOICE/FILL/BLANK/DRAWING/ESSAY/CALCULATION',
    weight DECIMAL(5,2) DEFAULT 1.0 COMMENT '题型权重',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_kp (knowledge_point_id),
    INDEX idx_type (question_type),
    UNIQUE KEY uk_kp_type (knowledge_point_id, question_type)
) COMMENT '知识点题型关联表';
```

**题型枚举值**：
| 题型代码 | 说明 |
|----------|------|
| CHOICE | 选择题 |
| FILL | 填空题 |
| BLANK | 简答题 |
| DRAWING | 画图题 |
| ESSAY | 作文题 |
| CALCULATION | 计算题 |

### 1.3 设计说明

**两种方案对比**：
1. **方案A（JSON字段）**：简单易实现，适合题型不经常变化的场景
2. **方案B（关联表）**：支持更细粒度的管理，每个知识点可以有不同题型权重

**推荐方案**：同时支持两种方案
- question_types JSON字段用于快速筛选
- knowledge_point_type 关联表用于细粒度权重管理

---

## 二、后端API接口设计

### 2.1 知识点题型管理

| 接口 | 方法 | 说明 | 请求参数 | 返回数据 |
|------|------|------|----------|----------|
| `/api/knowledge-point/type/list` | GET | 获取所有支持的题型 | - | 题型列表 |
| `/api/knowledge-point/by-type` | GET | 按题型获取知识点 | type(题型), subjectId, gradeId | 知识点列表 |
| `/api/knowledge-point/bind-type` | POST | 绑定知识点与题型 | knowledgePointId, questionTypes[] | 绑定结果 |
| `/api/knowledge-point/type/detail/{id}` | GET | 获取知识点的题型详情 | 知识点ID | 题型关联列表 |
| `/api/knowledge-point/type/update` | PUT | 更新知识点题型 | id, questionType, weight | 更新结果 |
| `/api/knowledge-point/type/delete/{id}` | DELETE | 删除知识点题型关联 | 关联ID | 删除结果 |

### 2.2 API详细设计

#### 2.2.1 获取题型列表

```
GET /api/knowledge-point/type/list

Response:
{
  "code": 200,
  "msg": "success",
  "data": [
    {"code": "CHOICE", "name": "选择题", "icon": "el-icon-circle-check"},
    {"code": "FILL", "name": "填空题", "icon": "el-icon-edit"},
    {"code": "BLANK", "name": "简答题", "icon": "el-icon-document"},
    {"code": "DRAWING", "name": "画图题", "icon": "el-icon-picture"},
    {"code": "ESSAY", "name": "作文题", "icon": "el-icon-write"},
    {"code": "CALCULATION", "name": "计算题", "icon": "el-icon-calculate"}
  ]
}
```

#### 2.2.2 按题型获取知识点

```
GET /api/knowledge-point/by-type?type=CHOICE&subjectId=1&gradeId=1

Request Params:
- type: 题型代码（必填）
- subjectId: 学科ID（可选）
- gradeId: 年级ID（可选）
- pageNum: 页码（默认1）
- pageSize: 每页数量（默认10）

Response:
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "力的概念",
        "subjectId": 1,
        "gradeId": 1,
        "questionTypes": ["CHOICE", "BLANK"],
        "examFreq": 5,
        "scoreWeight": 1.2
      }
    ],
    "total": 100,
    "pages": 10
  }
}
```

#### 2.2.3 绑定知识点与题型

```
POST /api/knowledge-point/bind-type

Request Body:
{
  "knowledgePointId": 1,
  "questionTypes": ["CHOICE", "BLANK", "DRAWING"],
  "weights": {
    "CHOICE": 1.0,
    "BLANK": 1.2,
    "DRAWING": 0.8
  }
}

Response:
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "knowledgePointId": 1,
    "questionTypes": ["CHOICE", "BLANK", "DRAWING"],
    "createdAt": "2026-03-25 10:00:00"
  }
}
```

#### 2.2.4 获取知识点题型详情

```
GET /api/knowledge-point/type/detail/1

Response:
{
  "code": 200,
  "msg": "success",
  "data": [
    {"id": 1, "knowledgePointId": 1, "questionType": "CHOICE", "weight": 1.0},
    {"id": 2, "knowledgePointId": 1, "questionType": "BLANK", "weight": 1.2},
    {"id": 3, "knowledgePointId": 1, "questionType": "DRAWING", "weight": 0.8}
  ]
}
```

#### 2.2.5 更新知识点题型权重

```
PUT /api/knowledge-point/type/update

Request Body:
{
  "id": 1,
  "questionType": "CHOICE",
  "weight": 1.5
}

Response:
{
  "code": 200,
  "msg": "success",
  "data": {"id": 1, "weight": 1.5}
}
```

#### 2.2.6 删除知识点题型关联

```
DELETE /api/knowledge-point/type/delete/1

Response:
{
  "code": 200,
  "msg": "删除成功"
}
```

### 2.3 知识点管理增强

现有的知识点API需要增强，支持题型筛选：

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/knowledge-point/list` | GET | 增加questionTypes筛选参数 |
| `/api/knowledge-point/tree` | GET | 树形返回知识点，增加题型信息 |
| `/api/knowledge-point` | POST | 创建知识点时支持题型 |
| `/api/knowledge-point/{id}` | PUT | 更新知识点时支持题型 |

---

## 三、前端页面设计

### 3.1 页面结构

新增知识点题型管理页面：

```
/views/teacher/KnowledgeType.vue  - 知识点题型管理（老师端）
```

### 3.2 功能设计

#### 3.2.1 知识点题型配置页面

**页面结构**：
```
┌─────────────────────────────────────────────────────────────┐
│ 知识点题型管理                              [刷新] [导出]   │
├─────────────────────────────────────────────────────────────┤
│ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│ │ 学科: 物理  │ │ 年级: 初二   │ │ 题型: 全部  │ [搜索]   │
│ └─────────────┘ └─────────────┘ └─────────────┘           │
├─────────────────────────────────────────────────────────────┤
│ 题型分布统计                                                │
│ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐   │
│ │选择题│ │填空题│ │简答题│ │画图题│ │作文题│ │计算题│   │
│ │ 45   │ │ 32   │ │ 18   │ │ 12   │ │  5   │ │ 28   │   │
│ └──────┘ └──────┘ └──────┘ └──────┘ └──────┘ └──────┘   │
├─────────────────────────────────────────────────────────────┤
│ 知识点列表                                      [新增]    │
│ ┌────────────────────────────────────────────────────────┐ │
│ │ 知识点名称        │ 题型                          │操作│
│ ├────────────────────────────────────────────────────────┤ │
│ │ 力的基本概念      │ [选择题] [填空题] [画图题]    │编辑│
│ │ 力的合成与分解    │ [选择题] [填空题]             │编辑│
│ │ 重力             │ [选择题] [填空题] [计算题]    │编辑│
│ └────────────────────────────────────────────────────────┘ │
│                                    [< 1 2 3 ... 10 >]      │
└─────────────────────────────────────────────────────────────┘
```

**交互说明**：
1. 顶部筛选：支持按学科/年级/题型筛选知识点
2. 题型分布统计：饼图展示各题型知识点数量
3. 知识点列表：显示知识点名称和关联的题型标签
4. 操作列：编辑按钮弹出题型配置弹窗

#### 3.2.2 题型配置弹窗

```
┌─────────────────────────────────────────────────┐
│ 编辑知识点题型                    [×]          │
├─────────────────────────────────────────────────┤
│ 知识点: 力的基本概念                        │
│ 学科: 物理    年级: 初二                      │
├─────────────────────────────────────────────────┤
│ 选择题型:                                     │
│ ☑ 选择题  ☑ 填空题  ☐ 简答题                │
│ ☑ 画图题  ☐ 作文题  ☐ 计算题                │
├─────────────────────────────────────────────────┤
│ 题型权重（可选）:                             │
│ 选择题: [1.0] 填空题: [1.2] 画图题: [0.8]    │
├─────────────────────────────────────────────────┤
│                         [取消] [保存]          │
└─────────────────────────────────────────────────┘
```

### 3.3 现有页面增强

#### 3.3.1 题目管理页面增强

题目管理页面增加题型筛选项，支持按题型筛选题目：

```
┌─────────────────────────────────────────────────────────────┐
│ 题目管理                                        [新增题目]  │
├─────────────────────────────────────────────────────────────┤
│ 学科: [物理 ▼] 年级: [初二 ▼] 题型: [选择题 ▼] [搜索]      │
├─────────────────────────────────────────────────────────────┤
│ 题目列表...                                               │
└─────────────────────────────────────────────────────────────┘
```

#### 3.3.2 知识点树形展示增强

知识点树形结构增加题型信息展示：

```
├── 力学
│   ├── 力的基本概念 [选择题] [填空题] [画图题]
│   ├── 力的合成与分解 [选择题] [填空题]
│   └── 牛顿第一定律 [选择题] [填空题] [计算题]
├── 光学
│   ├── 光的传播 [选择题] [填空题]
│   └── 光的反射 [选择题] [画图题]
```

### 3.4 组件设计

需要新增的Vue组件：

| 组件 | 路径 | 说明 |
|------|------|------|
| QuestionTypeSelect | @/components/QuestionTypeSelect | 题型多选组件 |
| QuestionTypeTag | @/components/QuestionTypeTag | 题型标签组件 |
| QuestionTypeStat | @/components/QuestionTypeStat | 题型统计组件 |

### 3.5 API调用

新增API调用函数：

```javascript
// api/knowledgePoint.js 新增

// 获取题型列表
export function getQuestionTypeList() {}

// 按题型获取知识点
export function getKnowledgeByType(params) {}

// 绑定知识点与题型
export function bindKnowledgeType(data) {}

// 获取知识点题型详情
export function getKnowledgeTypeDetail(id) {}

// 更新知识点题型权重
export function updateKnowledgeType(data) {}

// 删除知识点题型关联
export function deleteKnowledgeType(id) {}
```

---

## 四、实现建议

### 4.1 后端实现

1. **实体类**：已有KnowledgePoint和KnowledgePointType实体
2. **Mapper**：创建KnowledgePointTypeMapper
3. **Service**：创建KnowledgePointTypeService
4. **Controller**：在KnowledgePointController中新增题型相关接口

### 4.2 前端实现

1. 创建知识点题型管理页面
2. 增强现有知识点管理页面
3. 创建通用题型组件

### 4.3 数据初始化

系统初始化时，插入支持的题型数据：

```sql
INSERT INTO question_type (code, name, icon) VALUES 
('CHOICE', '选择题', 'el-icon-circle-check'),
('FILL', '填空题', 'el-icon-edit'),
('BLANK', '简答题', 'el-icon-document'),
('DRAWING', '画图题', 'el-icon-picture'),
('ESSAY', '作文题', 'el-icon-write'),
('CALCULATION', '计算题', 'el-icon-calculate');
```

---

## 五、待办清单

- [ ] architect: 确认设计方案 ✅
- [ ] backend: 实现API接口
- [ ] frontend: 实现知识点题型管理页面
- [ ] frontend: 增强题目管理和知识点管理页面
- [ ] tester: 测试验证

---

## 六、版本信息

*文档版本：v1.0*
*创建日期：2026-03-25*
*创建人：architect (Jerry团队)*

---

## 附录：数据库变更SQL汇总

```sql
-- 1. 知识点表增加字段
ALTER TABLE knowledge_point 
ADD COLUMN question_types JSON COMMENT '题型数组，如["CHOICE","BLANK"]',
ADD COLUMN exam_freq INT DEFAULT 0 COMMENT '历年考试出现频次',
ADD COLUMN score_weight DECIMAL(5,2) DEFAULT 1.0 COMMENT '得分权重';

-- 2. 新建知识点题型关联表
CREATE TABLE knowledge_point_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    knowledge_point_id BIGINT NOT NULL COMMENT '知识点ID',
    question_type VARCHAR(20) NOT NULL COMMENT '题型：CHOICE/FILL/BLANK/DRAWING/ESSAY/CALCULATION',
    weight DECIMAL(5,2) DEFAULT 1.0 COMMENT '题型权重',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_kp (knowledge_point_id),
    INDEX idx_type (question_type),
    UNIQUE KEY uk_kp_type (knowledge_point_id, question_type)
) COMMENT '知识点题型关联表';
```