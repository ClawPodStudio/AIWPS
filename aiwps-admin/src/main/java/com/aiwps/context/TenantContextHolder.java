package com.aiwps.context;

/**
 * 租户上下文持有者
 * 使用ThreadLocal存储当前请求的租户ID
 */
public class TenantContextHolder {
    
    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();
    
    public static void setTenantId(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }
    
    public static Long getCurrentTenantId() {
        return CURRENT_TENANT.get();
    }
    
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
