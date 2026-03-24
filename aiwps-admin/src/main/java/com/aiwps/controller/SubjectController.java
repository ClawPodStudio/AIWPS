package com.aiwps.controller;

import com.aiwps.service.SubjectService;
import com.aiwps.entity.Subject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subject")
public class SubjectController {
    
    @Autowired
    private SubjectService subjectService;
    
    @GetMapping("/list")
    public List<Subject> list() {
        LambdaQueryWrapper<Subject> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Subject::getSortOrder);
        return subjectService.list(wrapper);
    }
    
    @PostMapping
    public Map<String, Object> add(@RequestBody Subject subject) {
        subject.setCreatedAt(LocalDateTime.now());
        boolean success = subjectService.save(subject);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", subject);
        }
        return Map.of("code", 400, "msg", "添加失败");
    }
    
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Subject subject) {
        subject.setId(id);
        boolean success = subjectService.updateById(subject);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", subject);
        }
        return Map.of("code", 400, "msg", "更新失败");
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        boolean success = subjectService.removeById(id);
        if (success) {
            return Map.of("code", 200, "msg", "删除成功");
        }
        return Map.of("code", 400, "msg", "删除失败");
    }
}
