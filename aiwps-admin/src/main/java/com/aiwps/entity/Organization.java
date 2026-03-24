package com.aiwps.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("organization")
public class Organization {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private Long tenantId;
    private String province;
    private String city;
    private String address;
    private String contact;
    private String phone;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
