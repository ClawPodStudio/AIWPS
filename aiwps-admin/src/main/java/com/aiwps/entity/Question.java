package com.aiwps.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long subjectId;
    private Long gradeId;
    private String type;
    private String content;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String answer;
    private String analysis;
    private Integer difficulty;
    private String province;
    private Integer year;
    private String source;
    private String knowledgePointIds;
    private String tags;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
