package com.aiwps.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_mastery")
public class KnowledgeMastery {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long studentId;
    private Long knowledgePointId;
    
    // 掌握度 0-100
    private BigDecimal masteryLevel;
    
    // 正确/错误次数
    private Integer correctCount;
    private Integer wrongCount;
    
    // 复习时间
    private LocalDateTime lastReviewTime;
    private LocalDateTime nextReviewTime;
    
    // 是否从出题范围移除
    private Integer isRemoved;
    
    // 状态
    private Integer masteryStatus; // 0未掌握 1掌握中 2已掌握
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
