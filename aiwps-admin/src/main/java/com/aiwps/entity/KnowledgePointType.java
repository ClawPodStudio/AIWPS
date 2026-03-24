package com.aiwps.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_point_type")
public class KnowledgePointType {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long knowledgePointId;
    private String questionType; // 题型：CHOICE/FILL/BLANK/DRAWING/ESSAY/CALCULATION
    private BigDecimal weight; // 题型权重
    private LocalDateTime createdAt;
}
