package com.aiwps.controller;

import com.aiwps.entity.Organization;
import com.aiwps.service.OrganizationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/organization")
public class OrganizationController {
    
    @Autowired
    private OrganizationService organizationService;
    
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String province) {
        
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) wrapper.eq(Organization::getTenantId, tenantId);
        if (name != null && !name.isEmpty()) wrapper.like(Organization::getName, name);
        if (province != null && !province.isEmpty()) wrapper.eq(Organization::getProvince, province);
        
        Page<Organization> page = new Page<>(pageNum, pageSize);
        Page<Organization> result = organizationService.page(page, wrapper);
        
        return Map.of("code", 200, "msg", "success", "data",
                Map.of("records", result.getRecords(), "total", result.getTotal(),
                       "pages", result.getPages(), "current", result.getCurrent()));
    }
    
    @PostMapping
    public Map<String, Object> add(@RequestBody Organization organization) {
        boolean success = organizationService.save(organization);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", organization);
        }
        return Map.of("code", 400, "msg", "添加失败");
    }
    
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Organization organization) {
        organization.setId(id);
        boolean success = organizationService.updateById(organization);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", organization);
        }
        return Map.of("code", 400, "msg", "更新失败");
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        boolean success = organizationService.removeById(id);
        if (success) {
            return Map.of("code", 200, "msg", "删除成功");
        }
        return Map.of("code", 400, "msg", "删除失败");
    }
}
