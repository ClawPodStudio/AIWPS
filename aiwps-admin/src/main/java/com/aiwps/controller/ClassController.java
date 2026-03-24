package com.aiwps.controller;

import com.aiwps.entity.ClassInfo;
import com.aiwps.service.ClassService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/class")
public class ClassController {
    
    @Autowired
    private ClassService classService;
    
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) Long organizationId,
            @RequestParam(required = false) String name) {
        
        LambdaQueryWrapper<ClassInfo> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) wrapper.eq(ClassInfo::getTenantId, tenantId);
        if (gradeId != null) wrapper.eq(ClassInfo::getGradeId, gradeId);
        if (organizationId != null) wrapper.eq(ClassInfo::getOrganizationId, organizationId);
        if (name != null && !name.isEmpty()) wrapper.like(ClassInfo::getName, name);
        
        Page<ClassInfo> page = new Page<>(pageNum, pageSize);
        Page<ClassInfo> result = classService.page(page, wrapper);
        
        return Map.of("code", 200, "msg", "success", "data",
                Map.of("records", result.getRecords(), "total", result.getTotal(),
                       "pages", result.getPages(), "current", result.getCurrent()));
    }
    
    @PostMapping
    public Map<String, Object> add(@RequestBody ClassInfo classInfo) {
        boolean success = classService.save(classInfo);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", classInfo);
        }
        return Map.of("code", 400, "msg", "添加失败");
    }
    
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody ClassInfo classInfo) {
        classInfo.setId(id);
        boolean success = classService.updateById(classInfo);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", classInfo);
        }
        return Map.of("code", 400, "msg", "更新失败");
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        boolean success = classService.removeById(id);
        if (success) {
            return Map.of("code", 200, "msg", "删除成功");
        }
        return Map.of("code", 400, "msg", "删除失败");
    }
}
