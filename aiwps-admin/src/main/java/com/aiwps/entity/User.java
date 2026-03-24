package com.aiwps.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String username;
    private String password;
    private String role;
    private String name;
    private String mobile;
    private Long gradeId;
    private String subjectIds;
    private Long orgId;
    private Long classId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
