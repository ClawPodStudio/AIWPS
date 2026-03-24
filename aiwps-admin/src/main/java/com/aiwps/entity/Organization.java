package com.aiwps.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("organization")
public class Organization {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String name;
    private String type;
    private String province;
    private String contactName;
    private String contactMobile;
    private Integer status;
    private LocalDateTime createdAt;
}
