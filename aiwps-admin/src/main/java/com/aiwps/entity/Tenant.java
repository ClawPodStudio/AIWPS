package com.aiwps.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tenant")
public class Tenant {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String province;
    private String gradeRange;
    private String subjectRange;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
