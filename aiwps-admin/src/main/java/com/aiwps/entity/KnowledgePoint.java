package com.aiwps.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;
}
