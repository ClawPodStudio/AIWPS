package com.aiwps.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wrong_question_sort_config")
public class WrongQuestionSortConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long studentId;
    
    // 排序参数配置
    private BigDecimal examFreqWeight; // 考点频次权重
    private BigDecimal scoreWeight; // 得分权重
    private BigDecimal difficultyWeight; // 难度权重
    
    private Integer status; // 1启用 0禁用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
