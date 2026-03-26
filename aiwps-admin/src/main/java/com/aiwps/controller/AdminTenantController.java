package com.aiwps.controller;

import com.aiwps.context.TenantContextHolder;
import com.aiwps.entity.Tenant;
import com.aiwps.service.TenantService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 超级管理员租户管理接口
 */
@RestController
@RequestMapping("/api/admin/tenant")
public class AdminTenantController {
    
    @Autowired
    private TenantService tenantService;
    
    /**
     * 分页获取租户列表
     */
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        IPage<Tenant> pageResult = tenantService.adminList(page, size, name, status);
        return Map.of("code", 200, "msg", "success", "data",
                Map.of("records", pageResult.getRecords(), "total", pageResult.getTotal(),
                        "pages", pageResult.getPages(), "current", pageResult.getCurrent()));
    }
    
    /**
     * 获取租户详情，含关联的organization数量
     */
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Tenant tenant = tenantService.getAdminById(id);
        if (tenant == null) {
            return Map.of("code", 404, "msg", "租户不存在");
        }
        long orgCount = tenantService.countOrganizations(id);
        Map<String, Object> data = new HashMap<>();
        data.put("id", tenant.getId());
        data.put("name", tenant.getName());
        data.put("province", tenant.getProvince());
        data.put("gradeRange", tenant.getGradeRange());
        data.put("subjectRange", tenant.getSubjectRange());
        data.put("status", tenant.getStatus());
        data.put("createdAt", tenant.getCreatedAt());
        data.put("updatedAt", tenant.getUpdatedAt());
        data.put("orgCount", orgCount);
        return Map.of("code", 200, "msg", "success", "data", data);
    }
    
    /**
     * 创建租户
     */
    @PostMapping
    public Map<String, Object> create(@RequestBody Tenant tenant) {
        if (tenant.getName() == null || tenant.getName().isEmpty()) {
            return Map.of("code", 400, "msg", "租户名称不能为空");
        }
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setUpdatedAt(LocalDateTime.now());
        if (tenant.getStatus() == null) {
            tenant.setStatus(1);
        }
        boolean success = tenantService.adminCreate(tenant);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", tenant);
        }
        return Map.of("code", 400, "msg", "创建失败");
    }
    
    /**
     * 更新租户
     */
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Tenant tenant) {
        Tenant existing = tenantService.getAdminById(id);
        if (existing == null) {
            return Map.of("code", 404, "msg", "租户不存在");
        }
        if (tenant.getName() != null) existing.setName(tenant.getName());
        if (tenant.getProvince() != null) existing.setProvince(tenant.getProvince());
        if (tenant.getGradeRange() != null) existing.setGradeRange(tenant.getGradeRange());
        if (tenant.getSubjectRange() != null) existing.setSubjectRange(tenant.getSubjectRange());
        if (tenant.getStatus() != null) existing.setStatus(tenant.getStatus());
        existing.setUpdatedAt(LocalDateTime.now());
        boolean success = tenantService.adminUpdate(id, existing);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", existing);
        }
        return Map.of("code", 400, "msg", "更新失败");
    }
    
    /**
     * 删除租户
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Tenant existing = tenantService.getAdminById(id);
        if (existing == null) {
            return Map.of("code", 404, "msg", "租户不存在");
        }
        boolean success = tenantService.adminDelete(id);
        if (success) {
            return Map.of("code", 200, "msg", "删除成功");
        }
        return Map.of("code", 400, "msg", "删除失败");
    }
    
    /**
     * 获取当前租户上下文
     */
    @GetMapping("/current-context")
    public Map<String, Object> currentContext() {
        Long tenantId = TenantContextHolder.getCurrentTenantId();
        return Map.of("code", 200, "msg", "success", "data",
                Map.of("tenantId", tenantId != null ? tenantId : "none"));
    }
}
