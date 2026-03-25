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
        Organization existing = organizationService.getById(id);
        if (existing == null) {
            return Map.of("code", 404, "msg", "机构不存在");
        }
        // 只更新传入的字段
        if (organization.getName() != null) existing.setName(organization.getName());
        if (organization.getType() != null) existing.setType(organization.getType());
        if (organization.getProvince() != null) existing.setProvince(organization.getProvince());
        if (organization.getContactName() != null) existing.setContactName(organization.getContactName());
        if (organization.getContactMobile() != null) existing.setContactMobile(organization.getContactMobile());
        if (organization.getStatus() != null) existing.setStatus(organization.getStatus());
        
        boolean success = organizationService.updateById(existing);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", existing);
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
