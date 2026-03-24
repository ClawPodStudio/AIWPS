package com.aiwps.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_mastery")
public class KnowledgeMastery {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long knowledgePointId;
    private Integer masteryLevel;
    private Integer correctCount;
    private Integer totalCount;
    private LocalDateTime lastReviewTime;
    private Integer masteryStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
