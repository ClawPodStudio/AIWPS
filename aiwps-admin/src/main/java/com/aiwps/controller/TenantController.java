package com.aiwps.controller;

import com.aiwps.entity.Tenant;
import com.aiwps.service.TenantService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tenant")
public class TenantController {
    
    @Autowired
    private TenantService tenantService;
    
    @GetMapping("/list")
    public Map<String, Object> list() {
        List<Tenant> list = tenantService.list(new LambdaQueryWrapper<Tenant>()
                .orderByDesc(Tenant::getCreatedAt));
        return Map.of("code", 200, "msg", "success", "data", list);
    }
    
    @PostMapping
    public Map<String, Object> add(@RequestBody Tenant tenant) {
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setUpdatedAt(LocalDateTime.now());
        boolean success = tenantService.save(tenant);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", tenant);
        }
        return Map.of("code", 400, "msg", "添加失败");
    }
    
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Tenant tenant) {
        tenant.setId(id);
        tenant.setUpdatedAt(LocalDateTime.now());
        boolean success = tenantService.updateById(tenant);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", tenant);
        }
        return Map.of("code", 400, "msg", "更新失败");
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        boolean success = tenantService.removeById(id);
        if (success) {
            return Map.of("code", 200, "msg", "删除成功");
        }
        return Map.of("code", 400, "msg", "删除失败");
    }
}
