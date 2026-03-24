package com.aiwps.controller;

import com.aiwps.entity.WrongQuestion;
import com.aiwps.service.WrongQuestionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/wrong-question")
public class WrongQuestionController {
    
    @Autowired
    private WrongQuestionService wrongQuestionService;
    
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Integer isCorrected,
            @RequestParam(required = false) String status) {
        
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) wrapper.eq(WrongQuestion::getStudentId, studentId);
        if (isCorrected != null) wrapper.eq(WrongQuestion::getIsCorrected, isCorrected);
        if (status != null && !status.isEmpty()) wrapper.eq(WrongQuestion::getStatus, status);
        
        wrapper.orderByDesc(WrongQuestion::getCreatedAt);
        
        Page<WrongQuestion> page = new Page<>(pageNum, pageSize);
        Page<WrongQuestion> result = wrongQuestionService.page(page, wrapper);
        
        return Map.of("code", 200, "msg", "success", "data",
                Map.of("records", result.getRecords(), "total", result.getTotal(),
                       "pages", result.getPages(), "current", result.getCurrent()));
    }
    
    @GetMapping("/stats")
    public Map<String, Object> stats(@RequestParam(required = false) Long studentId) {
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) wrapper.eq(WrongQuestion::getStudentId, studentId);
        
        long totalWrong = wrongQuestionService.count(wrapper);
        
        // Count by subject
        Map<String, Object> subjectStats = new HashMap<>();
        
        return Map.of("code", 200, "msg", "success", "data", Map.of(
                "totalWrong", totalWrong,
                "subjectStats", subjectStats
        ));
    }
    
    @PostMapping
    public Map<String, Object> add(@RequestBody WrongQuestion wrongQuestion) {
        boolean success = wrongQuestionService.save(wrongQuestion);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", wrongQuestion);
        }
        return Map.of("code", 400, "msg", "添加失败");
    }
    
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody WrongQuestion wrongQuestion) {
        wrongQuestion.setId(id);
        boolean success = wrongQuestionService.updateById(wrongQuestion);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", wrongQuestion);
        }
        return Map.of("code", 400, "msg", "更新失败");
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        boolean success = wrongQuestionService.removeById(id);
        if (success) {
            return Map.of("code", 200, "msg", "删除成功");
        }
        return Map.of("code", 400, "msg", "删除失败");
    }
}
