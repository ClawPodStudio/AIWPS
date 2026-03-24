package com.aiwps.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("grade")
public class Grade {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String level;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
