package com.aiwps.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wrong_question")
public class WrongQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long questionId;
    private String wrongAnswer;
    private Integer isCorrected;
    private Integer wrongCount;
    private LocalDateTime lastWrongTime;
    private Integer status;
    
    // 智能排序字段
    private Integer examFreq; // 知识点考试频次
    private BigDecimal sortScore; // 综合排序分
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
