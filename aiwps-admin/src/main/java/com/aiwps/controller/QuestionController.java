package com.aiwps.controller;

import com.aiwps.entity.Question;
import com.aiwps.service.QuestionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/question")
public class QuestionController {
    
    @Autowired
    private QuestionService questionService;
    
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Integer status) {
        
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) wrapper.eq(Question::getTenantId, tenantId);
        if (subjectId != null) wrapper.eq(Question::getSubjectId, subjectId);
        if (gradeId != null) wrapper.eq(Question::getGradeId, gradeId);
        if (type != null && !type.isEmpty()) wrapper.eq(Question::getType, type);
        if (province != null && !province.isEmpty()) wrapper.eq(Question::getProvince, province);
        if (difficulty != null) wrapper.eq(Question::getDifficulty, difficulty);
        if (status != null) wrapper.eq(Question::getStatus, status);
        
        wrapper.orderByDesc(Question::getCreatedAt);
        
        Page<Question> page = new Page<>(pageNum, pageSize);
        Page<Question> result = questionService.page(page, wrapper);
        
        return Map.of("code", 200, "msg", "success", "data", 
                Map.of("records", result.getRecords(), "total", result.getTotal(), 
                       "pages", result.getPages(), "current", result.getCurrent()));
    }
    
    @PostMapping
    public Map<String, Object> add(@RequestBody Question question) {
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        boolean success = questionService.save(question);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", question);
        }
        return Map.of("code", 400, "msg", "添加失败");
    }
    
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Question question) {
        question.setId(id);
        question.setUpdatedAt(LocalDateTime.now());
        boolean success = questionService.updateById(question);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", question);
        }
        return Map.of("code", 400, "msg", "更新失败");
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        boolean success = questionService.removeById(id);
        if (success) {
            return Map.of("code", 200, "msg", "删除成功");
        }
        return Map.of("code", 400, "msg", "删除失败");
    }
}
