package com.aiwps.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("review_plan")
public class ReviewPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long knowledgePointId;
    private LocalDateTime planDate;
    private LocalDateTime reviewDate;
    private Integer reviewCount;
    private Integer totalCount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
