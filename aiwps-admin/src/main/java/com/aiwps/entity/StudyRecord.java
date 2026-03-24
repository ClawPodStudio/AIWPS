package com.aiwps.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("study_record")
public class StudyRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long questionId;
    private Long knowledgePointId;
    private String actionType;
    private Boolean isCorrect;
    private Integer timeSpent;
    private String ipAddress;
    private LocalDateTime createdAt;
}
