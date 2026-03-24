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
    public Map<String, Object> add(@RequestBody Map<String, Object> request) {
        Question question = new Question();
        question.setContent((String) request.get("content"));
        question.setSubjectId(request.get("subjectId") != null ? ((Number) request.get("subjectId")).longValue() : null);
        question.setGradeId(request.get("gradeId") != null ? ((Number) request.get("gradeId")).longValue() : null);
        question.setTenantId(1L);
        question.setType((String) request.get("type"));
        question.setDifficulty(request.get("difficulty") != null ? ((Number) request.get("difficulty")).intValue() : 1);
        question.setProvince((String) request.get("province"));
        question.setStatus(1);
        
        // Handle options
        Object optionsObj = request.get("options");
        String answerFromRequest = (String) request.get("answer");
        if (optionsObj instanceof List) {
            List<Map<String, Object>> options = (List<Map<String, Object>>) optionsObj;
            for (int i = 0; i < options.size() && i < 4; i++) {
                Map<String, Object> opt = options.get(i);
                String content = (String) opt.get("content");
                Boolean isCorrect = (Boolean) opt.get("isCorrect");
                switch (i) {
                    case 0: question.setOptionA(content); break;
                    case 1: question.setOptionB(content); break;
                    case 2: question.setOptionC(content); break;
                    case 3: question.setOptionD(content); break;
                }
                if (Boolean.TRUE.equals(isCorrect)) {
                    question.setAnswer("ABCD".substring(i, i + 1));
                }
            }
        }
        // 如果没有传answer字段，从请求体中取
        if (question.getAnswer() == null && answerFromRequest != null) {
            question.setAnswer(answerFromRequest);
        }
        // 如果答案还是空的，给默认值
        if (question.getAnswer() == null) {
            question.setAnswer("A");
        }
        
        question.setAnalysis((String) request.get("analysis"));
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        
        boolean success = questionService.save(question);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", question);
        }
        return Map.of("code", 400, "msg", "添加失败");
    }
    
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Question question = questionService.getById(id);
        if (question == null) {
            return Map.of("code", 404, "msg", "题目不存在");
        }
        
        if (request.containsKey("content")) question.setContent((String) request.get("content"));
        if (request.containsKey("subjectId")) question.setSubjectId(((Number) request.get("subjectId")).longValue());
        if (request.containsKey("gradeId")) question.setGradeId(((Number) request.get("gradeId")).longValue());
        if (request.containsKey("type")) question.setType((String) request.get("type"));
        if (request.containsKey("answer")) question.setAnswer((String) request.get("answer"));
        if (request.containsKey("analysis")) question.setAnalysis((String) request.get("analysis"));
        if (request.containsKey("difficulty")) question.setDifficulty(((Number) request.get("difficulty")).intValue());
        
        question.setUpdatedAt(LocalDateTime.now());
        boolean success = questionService.updateById(question);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", question);
        }
        return Map.of("code", 400, "msg", "更新失败");
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Question q = questionService.getById(id);
        if (q == null) {
            return Map.of("code", 404, "msg", "题目不存在");
        }
        boolean success = questionService.removeById(id);
        if (success) {
            return Map.of("code", 200, "msg", "删除成功");
        }
        return Map.of("code", 400, "msg", "删除失败");
    }
}
