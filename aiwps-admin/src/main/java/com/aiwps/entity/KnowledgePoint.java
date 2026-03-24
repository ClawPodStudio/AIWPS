package com.aiwps.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("knowledge_point")
public class KnowledgePoint {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long subjectId;
    private Long gradeId;
    private Long parentId;
    private String name;
    private Integer level;
    private Integer sortOrder;
    
    // 题型分类字段
    private String questionTypes; // JSON数组，如 ["CHOICE","BLANK"]
    private Integer examFreq; // 历年考试出现频次
    private java.math.BigDecimal scoreWeight; // 得分权重
    
    private LocalDateTime createdAt;
}
