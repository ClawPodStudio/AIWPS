package com.aiwps.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("paper_question")
public class PaperQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long paperId;
    private Long questionId;
    @TableField("sort_order")
    private Integer questionOrder;
    private Integer score;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
