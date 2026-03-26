package com.aiwps.interceptor;

import com.aiwps.annotation.TenantFilter;
import com.aiwps.context.TenantContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 租户上下文拦截器
 * 从请求header【X-Tenant-ID】读取tenantId，存入ThreadLocal
 */
@Component
public class TenantContextInterceptor implements HandlerInterceptor {
    
    private static final String TENANT_HEADER = "X-Tenant-ID";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            TenantFilter annotation = handlerMethod.getMethodAnnotation(TenantFilter.class);
            if (annotation == null) {
                annotation = handlerMethod.getBeanType().getAnnotation(TenantFilter.class);
            }
            
            // 只有标注了@TenantFilter的方法才需要设置租户上下文
            if (annotation != null) {
                String tenantIdStr = request.getHeader(TENANT_HEADER);
                if (tenantIdStr != null && !tenantIdStr.isEmpty()) {
                    try {
                        Long tenantId = Long.parseLong(tenantIdStr);
                        TenantContextHolder.setTenantId(tenantId);
                    } catch (NumberFormatException e) {
                        // 忽略无效的tenantId
                    }
                }
            }
        }
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束后清除租户上下文
        TenantContextHolder.clear();
    }
}
