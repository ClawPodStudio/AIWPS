package com.aiwps.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("variant_question")
public class VariantQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long originalQuestionId;      // 原题ID
    private Long variantQuestionId;       // 变形题ID
    private Long studentId;               // 生成给哪个学生
    private Long knowledgePointId;        // 关联知识点
    
    private String variantType;          // NUMBER_CHANGE/PHRASE_CHANGE/CONTEXT_CHANGE/OPTION_SHUFFLE
    private String generatedBy;           // AI/MANUAL
    private Integer status;               // 0待审核 1已通过 2已使用
    
    private LocalDateTime createdAt;
}
