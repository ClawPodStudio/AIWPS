package com.aiwps.annotation;

import java.lang.annotation.*;

/**
 * 租户过滤注解
 * 标注在Controller或方法上，表示该接口需要过滤当前租户的数据
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantFilter {
}
