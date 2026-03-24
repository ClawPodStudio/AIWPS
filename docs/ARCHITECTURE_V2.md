# AIWPS 初高中个性化辅导SaaS系统 - 架构设计文档V2

## 一、题型抽象扩展设计

### 1.1 题型体系架构

#### 1.1.1 题型分类模型

```
┌─────────────────────────────────────────────────────────────────┐
│                        题型体系                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│                        ┌──────────────┐                         │
│                        │ QuestionType │ (题型基类)              │
│                        │   题型基类    │                         │
│                        └──────┬───────┘                         │
│                               │                                  │
│         ┌─────────────────────┼─────────────────────┐         │
│         │                     │                     │         │
│  ┌──────▼──────┐      ┌──────▼──────┐      ┌───────▼─────┐     │
│  │  Objective  │      │  Subjective │      │  Composite  │     │
│  │   客观题    │      │   主观题     │      │   复合题    │     │
│  └──────┬──────┘      └──────┬──────┘      └──────┬──────┘     │
│         │                   │                   │             │
│    ┌────┴────┐         ┌─────┴─────┐        ┌────┴────┐        │
│    │         │         │           │        │         │        │
│ ┌──▼──┐ ┌───▼─┐   ┌────▼────┐  ┌───▼───┐ ┌──▼────┐ ┌─▼────┐  │
│ │单选 │ │多选 │   │  填空   │  │  问答 │ │完形  │ │阅读  │  │
│ └────┘ └─────┘   └─────────┘  └───────┘ └──────┘ └──────┘  │
│                                                                  │
│ ┌──┐ ┌──┐ ┌───┐  ┌───────┐ ┌───────┐ ┌────┐ ┌────┐ ┌────┐  │
│ │判 │ │填 │ │计算│  │ 证明  │ │实验题 │ │作  │ │计算│ │证明│  │
│ │断 │ │空 │ │  │  │       │ │       │ │文  │ │题  │ │题  │  │
│ └──┘ └──┘ └───┘  └───────┘ └───────┘ └────┘ └────┘ └────┘  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

#### 1.1.2 题型枚举值定义

```java
public enum QuestionType {
    // 通用题型
    SINGLE_CHOICE("SINGLE_CHOICE", "单选题", "OBJECTIVE", true),
    MULTIPLE_CHOICE("MULTIPLE_CHOICE", "多选题", "OBJECTIVE", true),
    TRUE_FALSE("TRUE_FALSE", "判断题", "OBJECTIVE", true),
    BLANK("BLANK", "填空题", "OBJECTIVE", true),
    
    // 英语特定题型
    CLOZE("CLOZE", "完形填空", "OBJECTIVE", true),
    READING("READING", "阅读理解", "OBJECTIVE", true),
    WRITING("WRITING", "写作", "SUBJECTIVE", false),
    LISTENING("LISTENING", "听力", "OBJECTIVE", true),
    
    // 数学题型
    CALCULATION("CALCULATION", "计算题", "SUBJECTIVE", false),
    PROOF("PROOF", "证明题", "SUBJECTIVE", false),
    APPLICATION("APPLICATION", "应用题", "SUBJECTIVE", false),
    
    // 语文题型
    CHINESE_READING("CHINESE_READING", "阅读理解", "SUBJECTIVE", false),
    COMPOSITION("COMPOSITION", "作文", "SUBJECTIVE", false),
    POEM_APPRECIATION("POEM_APPRECIATION", "诗词鉴赏", "SUBJECTIVE", false),
    
    // 理综题型
    PHYSICS_CALC("PHYSICS_CALC", "物理计算题", "SUBJECTIVE", false),
    PHYSICS_EXPERIMENT("PHYSICS_EXPERIMENT", "物理实验题", "SUBJECTIVE", false),
    CHEMISTRY_CALC("CHEMISTRY_CALC", "化学计算题", "SUBJECTIVE", false),
    CHEMISTRY_EXPERIMENT("CHEMISTRY_EXPERIMENT", "化学实验题", "SUBJECTIVE", false),
    BIOLOGY_EXPERIMENT("BIOLOGY_EXPERIMENT", "生物实验题", "SUBJECTIVE", false);
    
    private final String code;
    private final String name;
    private final String category; // OBJECTIVE/SUBJECTIVE/COMPOSITE
    private final boolean autoGrading; // 是否支持自动判分
    
    // 题型分类
    public String getCategory() {
        return this.category;
    }
    
    // 是否可自动判分
    public boolean isAutoGrading() {
        return this.autoGrading;
    }
}
```

### 1.2 题型数据库设计

#### 1.2.1 题型表 (question_type)

```sql
CREATE TABLE IF NOT EXISTS question_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '题型代码',
    name VARCHAR(50) NOT NULL COMMENT '题型名称',
    category VARCHAR(20) NOT NULL COMMENT 'OBJECTIVE/SUBJECTIVE/COMPOSITE',
    auto_grading TINYINT DEFAULT 1 COMMENT '是否支持自动判分 0否 1是',
    scoring_rules TEXT COMMENT '判分规则(JSON)',
    display_config TEXT COMMENT '显示配置(JSON)',
    subject_ids VARCHAR(200) COMMENT '适用学科ID,逗号分隔 空表示通用',
    grade_ids VARCHAR(200) COMMENT '适用年级ID,逗号分隔',
    icon VARCHAR(100) COMMENT '题型图标',
    color VARCHAR(20) COMMENT '主题色',
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code (code),
    INDEX idx_category (category),
    INDEX idx_subject (subject_ids)
) COMMENT '题型表';
```

#### 1.2.2 题目表扩展 (question)

```sql
-- 扩展question表字段
ALTER TABLE question ADD COLUMN type VARCHAR(50) NOT NULL COMMENT '题型代码';
ALTER TABLE question ADD COLUMN type_category VARCHAR(20) COMMENT '题型分类 OBJECTIVE/SUBJECTIVE';
ALTER TABLE question ADD COLUMN sub_questions TEXT COMMENT '子题列表(JSON) 复合题使用';
ALTER TABLE question ADD COLUMN scoring_rule VARCHAR(20) DEFAULT 'EXACT' COMMENT '判分规则 EXACT/PARTIAL/STEP';
ALTER TABLE question ADD COLUMN scoring_config TEXT COMMENT '判分配置(JSON)';

-- 示例：完形填空题目内容JSON结构
{
  "type": "CLOZE",
  "article": "短文内容...",
  "blanks": [
    {
      "position": 1,
      "blankIndex": 1,
      "options": ["A. xxx", "B. xxx", "C. xxx", "D. xxx"],
      "answer": "A",
      "analysis": "解析..."
    }
  ]
}

-- 示例：阅读理解题目内容JSON结构
{
  "type": "READING",
  "article": "阅读材料内容...",
  "questions": [
    {
      "questionId": 1,
      "content": "问题1内容",
      "options": ["A", "B", "C", "D"],
      "answer": "A",
      "score": 2
    }
  ]
}
```

### 1.3 判分逻辑设计

#### 1.3.1 判分策略接口

```java
public interface QuestionGradingStrategy {
    
    /**
     * 自动判分
     * @param question 题目
     * @param studentAnswer 学生答案
     * @return 判分结果
     */
    GradingResult grade(Question question, String studentAnswer);
    
    /**
     * 获取得分详情
     * @return 得分明细
     */
    List<ScoreDetail> getScoreDetails();
}
```

#### 1.3.2 判分策略实现

```java
// 客观题判分策略
@Component
public class ObjectiveGradingStrategy implements QuestionGradingStrategy {
    
    @Override
    public GradingResult grade(Question question, String studentAnswer) {
        String correctAnswer = question.getAnswer();
        
        if (question.getType().equals("MULTIPLE_CHOICE")) {
            // 多选题：需要完全匹配
            Set<String> correctSet = new HashSet<>(Arrays.asList(correctAnswer.split(",")));
            Set<String> answerSet = new HashSet<>(Arrays.asList(studentAnswer.split(",")));
            boolean isCorrect = correctSet.equals(answerSet);
            return new GradingResult(isCorrect ? 100 : 0, isCorrect);
        } else {
            // 单选/判断/填空：精确匹配
            boolean isCorrect = correctAnswer.equalsIgnoreCase(studentAnswer.trim());
            return new GradingResult(isCorrect ? 100 : 0, isCorrect);
        }
    }
}

// 主观题判分策略
@Component
public class SubjectiveGradingStrategy implements QuestionGradingStrategy {
    
    @Override
    public GradingResult grade(Question question, String studentAnswer) {
        // 主观题默认返回待批改状态
        return new GradingResult(-1, false, GradingStatus.PENDING_MANUAL);
    }
}

// 复合题判分策略
@Component
public class CompositeGradingStrategy implements QuestionGradingStrategy {
    
    @Override
    public GradingResult grade(Question question, String studentAnswer) {
        // 解析子题，逐个判分
        List<SubQuestion> subQuestions = JSON.parseArray(
            question.getSubQuestions(), SubQuestion.class
        );
        List<ScoreDetail> details = new ArrayList<>();
        int totalScore = 0;
        
        for (SubQuestion sq : subQuestions) {
            String subAnswer = extractSubAnswer(studentAnswer, sq.getIndex());
            boolean correct = sq.getAnswer().equals(subAnswer);
            int score = correct ? sq.getScore() : 0;
            totalScore += score;
            details.add(new ScoreDetail(sq.getIndex(), score, sq.getScore(), correct));
        }
        
        return new GradingResult(totalScore, details);
    }
}
```

### 1.4 题目内容JSON结构

#### 1.4.1 选择题/判断题

```json
{
  "type": "SINGLE_CHOICE",
  "content": "下列关于光的说法正确的是：",
  "options": {
    "A": "光在真空中传播速度最快",
    "B": "光在不同介质中传播速度相同",
    "C": "光沿直线传播",
    "D": "光不需要介质就能传播"
  },
  "answer": "C",
  "analysis": "光在不同介质中的传播速度不同，在真空中最快。光沿直线传播。",
  "score": 5
}
```

#### 1.4.2 完形填空

```json
{
  "type": "CLOZE",
  "content": {
    "article": "阅读下面的短文，掌握其大意，然后从短文后各题所给的A、B、C、D四个选项中选出最佳选项。\n\nMike is a student. He ___1___ at No.2 Middle School. Every day he ___2___ to school at 7:00 in the morning.",
    "blanks": [
      {
        "index": 1,
        "position": "He ___1___ at No.2 Middle School.",
        "options": ["A. studies", "B. studies", "C. studies", "D. studies"],
        "answer": "A",
        "score": 2,
        "analysis": "第三人称单数动词加s"
      },
      {
        "index": 2,
        "position": "Every day he ___2___ to school",
        "options": ["A. goes", "B. goes", "C. goes", "D. goes"],
        "answer": "A",
        "score": 2,
        "analysis": "固定搭配go to school"
      }
    ]
  },
  "totalScore": 30
}
```

#### 1.4.3 阅读理解

```json
{
  "type": "READING",
  "content": {
    "article": "阅读短文，回答下面各题。\n\nThe mobile phone is a very useful tool in our daily life. It can help us communicate with each other easily...",
    "questions": [
      {
        "index": 1,
        "question": "What is the passage mainly about?",
        "options": ["A. The history of mobile phones", "B. The uses of mobile phones", "C. The disadvantages of mobile phones", "D. The advantages of mobile phones"],
        "answer": "B",
        "score": 3
      },
      {
        "index": 2,
        "question": "According to the passage, what can mobile phones NOT do?",
        "options": ["A. Make calls", "B. Send messages", "C. Cook meals", "D. Take photos"],
        "answer": "C",
        "score": 3
      }
    ]
  },
  "totalScore": 30
}
```

#### 1.4.4 作文题

```json
{
  "type": "COMPOSITION",
  "content": {
    "title": "My Future Dream",
    "requirement": "请根据以下提示，写一篇不少于80词的英语短文。\n1. What do you want to be in the future?\n2. Why do you want to be a...?\n3. How are you going to achieve your dream?",
    "wordLimit": 80,
    "totalScore": 25,
    "scoringCriteria": [
      {"criterion": "内容完整", "maxScore": 10},
      {"criterion": "语法正确", "maxScore": 8},
      {"criterion": "词汇丰富", "maxScore": 7}
    ]
  },
  "allowUpload": true,
  "allowImage": true
}
```

#### 1.4.5 数学计算题/证明题

```json
{
  "type": "CALCULATION",
  "content": {
    "question": "计算下列各题：\n(1) 23 + 17 = ?\n(2) 45 × 6 = ?",
    "steps": [
      {
        "step": 1,
        "content": "23 + 17 = 40",
        "score": 5
      },
      {
        "step": 2,
        "content": "45 × 6 = 270",
        "score": 5
      }
    ],
    "finalAnswer": "40, 270"
  },
  "allowStepScoring": true,
  "totalScore": 10
}
```

---

## 二、试卷二维码 + 机构权限设计

### 2.1 架构设计

#### 2.1.1 二维码业务流程

```
┌─────────────────────────────────────────────────────────────────────┐
│                      试卷二维码生成与验证流程                          │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│   ┌─────────┐      ┌──────────┐      ┌─────────────┐                │
│   │ 教师端  │      │ 生成二维码 │      │   数据库    │                │
│   │ 创建试卷 │ ───► │ 生成Token │ ───► │ 存储Token   │                │
│   └─────────┘      └────┬─────┘      └─────────────┘                │
│                         │                                            │
│                         │ QR Code                                    │
│                         ▼                                            │
│                    ┌─────────┐                                       │
│                    │  二维码  │                                       │
│                    └────┬────┘                                       │
│                         │                                            │
│                         │ 手机扫码                                    │
│                         ▼                                            │
│   ┌─────────────────────────────────────────────────────────────┐    │
│   │                     手机H5页面                              │    │
│   │  ┌───────────┐  ┌───────────┐  ┌───────────┐               │    │
│   │  │ 解析Token │→│ 验证机构  │→│ 展示结果  │               │    │
│   │  │  解码URL  │  │ 权限校验  │  │ 机构信息  │               │    │
│   │  └───────────┘  └───────────┘  └───────────┘               │    │
│   └─────────────────────────────────────────────────────────────┘    │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2 Token生成算法

#### 2.2.1 Token结构

```
Token = Base64(加密(JSON({
    "paperId": "试卷ID",
    "orgId": "机构ID", 
    "expireTime": "过期时间戳",
    "nonce": "随机字符串",
    "signature": "签名"
})))
```

#### 2.2.2 Token生成代码

```java
public class PaperQRCodeToken {
    
    private static final String SECRET_KEY = "AIWPS_QR_SECRET_KEY"; // 密钥配置化
    private static final long DEFAULT_EXPIRE_MINUTES = 60; // 默认1小时
    
    /**
     * 生成二维码Token
     */
    public static String generateToken(PaperQRCodeRequest request) {
        long expireTime = System.currentTimeMillis() + 
            (request.getExpireMinutes() > 0 ? 
                request.getExpireMinutes() * 60 * 1000 : 
                DEFAULT_EXPIRE_MINUTES * 60 * 1000);
        
        String nonce = UUID.randomUUID().toString().replace("-", "");
        
        // 构建Payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("paperId", request.getPaperId());
        payload.put("orgId", request.getOrgId());
        payload.put("expireTime", expireTime);
        payload.put("nonce", nonce);
        
        // 生成签名
        String signature = generateSignature(payload, SECRET_KEY);
        payload.put("signature", signature);
        
        // Base64编码
        String json = JSON.toJSONString(payload);
        return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 生成签名
     */
    private static String generateSignature(Map<String, Object> payload, String secret) {
        // 按key排序后拼接
        StringBuilder sb = new StringBuilder();
        payload.keySet().stream().sorted().forEach(key -> {
            sb.append(key).append("=").append(payload.get(key)).append("&");
        });
        sb.append("key=").append(secret);
        
        // HMAC-SHA256签名
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(sb.toString().getBytes());
            return bytesToHex(hmacBytes);
        } catch (Exception e) {
            throw new RuntimeException("签名失败", e);
        }
    }
    
    /**
     * 验证Token
     */
    public static QRCodeTokenPayload verifyToken(String token) {
        try {
            // Base64解码
            String json = new String(Base64.getUrlDecoder().decode(token));
            QRCodeTokenPayload payload = JSON.parseObject(json, QRCodeTokenPayload.class);
            
            // 验证过期时间
            if (System.currentTimeMillis() > payload.getExpireTime()) {
                throw new BusinessException("二维码已过期");
            }
            
            // 验证签名
            Map<String, Object> data = new HashMap<>();
            data.put("paperId", payload.getPaperId());
            data.put("orgId", payload.getOrgId());
            data.put("expireTime", payload.getExpireTime());
            data.put("nonce", payload.getNonce());
            
            String expectedSignature = generateSignature(data, SECRET_KEY);
            if (!expectedSignature.equals(payload.getSignature())) {
                throw new BusinessException("签名验证失败");
            }
            
            return payload;
        } catch (Exception e) {
            throw new BusinessException("Token无效: " + e.getMessage());
        }
    }
}

@Data
public class QRCodeTokenPayload {
    private Long paperId;
    private Long orgId;
    private Long expireTime;
    private String nonce;
    private String signature;
}
```

### 2.3 二维码API设计

#### 2.3.1 生成试卷二维码

```
POST /api/v1/papers/{paperId}/qrcode
```

**请求头**:
```
Authorization: Bearer {token}
```

**请求参数**:
```json
{
  "orgId": 1,
  "expireMinutes": 60,
  "width": 300,
  "height": 300
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "qrcodeUrl": "data:image/png;base64,iVBORw0KGgo...",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expireTime": "2024-03-25 10:30:00",
    "qrcodeContent": "https://aiwps.com/qr/verify?token=eyJ..."
  }
}
```

#### 2.3.2 扫码验证接口（H5前端调用）

```
GET /api/v1/qr/verify
```

**Query参数**:
```
token: 二维码Token
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "valid": true,
    "paper": {
      "id": 1,
      "title": "2024年中考数学模拟卷",
      "subject": "数学",
      "grade": "初三",
      "totalScore": 120,
      "questionCount": 25
    },
    "org": {
      "id": 1,
      "name": "北京市第一中学",
      "type": "SCHOOL"
    },
    "expireTime": "2024-03-25 10:30:00"
  }
}
```

**验证失败响应**:
```json
{
  "code": 402,
  "message": "二维码已过期",
  "data": {
    "valid": false,
    "reason": "EXPIRED"
  }
}
```

#### 2.3.3 获取机构学生信息（扫码后）

```
GET /api/v1/qr/org-info
```

**Query参数**:
```
token: 二维码Token
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "org": {
      "id": 1,
      "name": "北京市第一中学",
      "province": "北京市",
      "city": "北京市",
      "district": "海淀区",
      "type": "SCHOOL",
      "studentCount": 1200,
      "teacherCount": 80
    },
    "availableClasses": [
      {"id": 1, "name": "初三(1)班", "studentCount": 45},
      {"id": 2, "name": "初三(2)班", "studentCount": 42}
    ]
  }
}
```

#### 2.3.4 机构授权确认接口

```
POST /api/v1/qr/authorize
```

**请求体**:
```json
{
  "token": "二维码Token",
  "orgId": 1,
  "classIds": [1, 2],
  "action": "VIEW_PRACTICE" // VIEW_PRACTICE / PRACTICE / DOWNLOAD
}
```

**响应**:
```json
{
  "code": 200,
  "message": "授权成功",
  "data": {
    "authCode": "AUTH_20240325_XXXXXX"
  }
}
```

### 2.4 二维码存储表设计

#### 2.4.1 试卷二维码表

```sql
CREATE TABLE IF NOT EXISTS paper_qrcode (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    paper_id BIGINT NOT NULL,
    org_id BIGINT NOT NULL COMMENT '授权机构ID',
    token VARCHAR(500) NOT NULL COMMENT 'Token',
    token_expire_time DATETIME NOT NULL COMMENT 'Token过期时间',
    qrcode_url VARCHAR(500) COMMENT '二维码图片URL',
    qrcode_content VARCHAR(500) COMMENT '二维码内容(URL)',
    width INT DEFAULT 300,
    height INT DEFAULT 300,
    view_count INT DEFAULT 0 COMMENT '查看次数',
    auth_count INT DEFAULT 0 COMMENT '授权次数',
    status TINYINT DEFAULT 1 COMMENT '1有效 2已失效',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_paper (paper_id),
    INDEX idx_org (org_id),
    INDEX idx_token (token),
    INDEX idx_expire (token_expire_time)
) COMMENT '试卷二维码表';
```

#### 2.4.2 机构授权记录表

```sql
CREATE TABLE IF NOT EXISTS org_auth_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    qrcode_id BIGINT NOT NULL,
    org_id BIGINT NOT NULL,
    class_ids VARCHAR(200) COMMENT '授权班级ID,逗号分隔',
    action VARCHAR(50) NOT NULL COMMENT '授权操作',
    auth_code VARCHAR(50) UNIQUE COMMENT '授权码',
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    status TINYINT DEFAULT 1 COMMENT '1有效 2已取消',
    expire_time DATETIME COMMENT '授权过期时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_org (org_id),
    INDEX idx_auth_code (auth_code)
) COMMENT '机构授权记录表';
```

### 2.5 二维码内容URL格式

#### 2.5.1 二维码内容

```
https://aiwps.com/qr/verify?token={Base64EncodedToken}
```

或简洁版（服务端解析）：

```
aiwps://qr/verify/{token}
```

#### 2.5.2 H5验证页面流程

```
┌─────────────────────────────────────────────────────────────────┐
│                      H5验证页面流程                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. 页面加载                                                      │
│     │                                                            │
│     ▼                                                            │
│  2. 从URL解析token参数                                            │
│     │                                                            │
│     ▼                                                            │
│  3. 调用 /api/v1/qr/verify 验证token                              │
│     │                                                            │
│     ├─ 成功 ──► 4. 显示试卷信息 + 机构授权按钮                      │
│     │                                                            │
│     └─ 失败 ──4. 显示错误原因 + 重新获取提示                         │
│                                                                  │
│  5. 用户点击"授权使用"                                             │
│     │                                                            │
│     ▼                                                            │
│  6. 调用 /api/v1/qr/authorize 确认授权                             │
│     │                                                            │
│     ▼                                                            │
│  7. 显示授权成功 + 可执行操作                                       │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 三、版本记录

| 版本 | 日期 | 说明 | 作者 |
|------|------|------|------|
| v1.0 | 2024-03-24 | 初版架构设计 | architect |
| v2.0 | 2024-03-25 | 题型抽象扩展 + 试卷二维码架构 | architect |
