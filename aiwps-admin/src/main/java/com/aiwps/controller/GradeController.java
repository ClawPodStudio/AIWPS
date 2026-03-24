package com.aiwps.controller;

import com.aiwps.service.GradeService;
import com.aiwps.entity.Grade;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grade")
public class GradeController {
    
    @Autowired
    private GradeService gradeService;
    
    @GetMapping("/list")
    public List<Grade> list() {
        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Grade::getSortOrder);
        return gradeService.list(wrapper);
    }
    
    @PostMapping
    public Map<String, Object> add(@RequestBody Grade grade) {
        grade.setCreatedAt(LocalDateTime.now());
        boolean success = gradeService.save(grade);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", grade);
        }
        return Map.of("code", 400, "msg", "添加失败");
    }
    
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Grade grade) {
        grade.setId(id);
        boolean success = gradeService.updateById(grade);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", grade);
        }
        return Map.of("code", 400, "msg", "更新失败");
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        boolean success = gradeService.removeById(id);
        if (success) {
            return Map.of("code", 200, "msg", "删除成功");
        }
        return Map.of("code", 400, "msg", "删除失败");
    }
}
