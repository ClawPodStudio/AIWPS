package com.aiwps.ai.controller;

import com.aiwps.ai.dto.AiQuestionRequest;
import com.aiwps.ai.service.AiQuestionService;
import com.aiwps.ai.service.EbbinghausService;
import com.aiwps.entity.Question;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai/question")
public class AiQuestionController {
    
    @Autowired
    private AiQuestionService aiQuestionService;
    
    @Autowired
    private EbbinghausService ebbinghausService;
    
    /**
     * AI智能出题
     */
    @PostMapping("/generate")
    public Map<String, Object> generateQuestions(@RequestBody AiQuestionRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (request.getCount() == null || request.getCount() <= 0) {
                request.setCount(5); // 默认5道题
            }
            
            List<Question> questions = aiQuestionService.generateQuestions(request);
            
            result.put("success", true);
            result.put("message", "AI出题成功");
            result.put("data", questions);
            result.put("count", questions.size());
        } catch (Exception e) {
            log.error("AI出题失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "AI出题失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 知识点关联出题
     */
    @PostMapping("/generate/by-knowledge")
    public Map<String, Object> generateByKnowledgePoints(
            @RequestParam List<Long> knowledgePointIds,
            @RequestParam(defaultValue = "5") Integer count) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Question> questions = aiQuestionService.generateQuestionsByKnowledgePoints(knowledgePointIds, count);
            
            result.put("success", true);
            result.put("message", "知识点关联出题成功");
            result.put("data", questions);
            result.put("count", questions.size());
        } catch (Exception e) {
            log.error("知识点关联出题失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "知识点关联出题失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 变形题生成
     */
    @PostMapping("/generate/transform")
    public Map<String, Object> generateTransformQuestions(
            @RequestParam Long baseQuestionId,
            @RequestParam(required = false) String transformType,
            @RequestParam(defaultValue = "3") Integer count) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (transformType == null || transformType.isEmpty()) {
                transformType = "number"; // 默认数字变化
            }
            
            List<Question> questions = aiQuestionService.generateTransformQuestions(
                    baseQuestionId, transformType, count);
            
            result.put("success", true);
            result.put("message", "变形题生成成功");
            result.put("data", questions);
            result.put("count", questions.size());
        } catch (Exception e) {
            log.error("变形题生成失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "变形题生成失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 基于艾宾浩斯记忆曲线出题
     */
    @PostMapping("/generate/ebbinghaus")
    public Map<String, Object> generateWithEbbinghaus(
            @RequestParam Long studentId,
            @RequestParam Long subjectId,
            @RequestParam(defaultValue = "10") Integer count) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Question> questions = aiQuestionService.generateQuestionsWithEbbinghaus(
                    studentId, subjectId, count);
            
            result.put("success", true);
            result.put("message", "艾宾浩斯记忆曲线出题成功");
            result.put("data", questions);
            result.put("count", questions.size());
        } catch (Exception e) {
            log.error("艾宾浩斯出题失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "艾宾浩斯出题失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 生成试卷
     */
    @PostMapping("/generate/exam")
    public Map<String, Object> generateExamPaper(@RequestBody AiQuestionRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (request.getCount() == null || request.getCount() <= 0) {
                request.setCount(20); // 默认20道题
            }
            
            List<Question> questions = aiQuestionService.generateExamPaper(request);
            
            result.put("success", true);
            result.put("message", "试卷生成成功");
            result.put("data", questions);
            result.put("count", questions.size());
        } catch (Exception e) {
            log.error("试卷生成失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "试卷生成失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取复习建议
     */
    @GetMapping("/review/suggestion")
    public Map<String, Object> getReviewSuggestion(@RequestParam Integer masteryLevel) {
        Map<String, Object> result = new HashMap<>();
        
        String suggestion = ebbinghausService.getReviewSuggestion(masteryLevel);
        
        result.put("success", true);
        result.put("data", suggestion);
        
        return result;
    }
    
    /**
     * 测试AI连接
     */
    @GetMapping("/test")
    public Map<String, Object> testConnection() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "AI出题引擎已就绪");
        return result;
    }
}
