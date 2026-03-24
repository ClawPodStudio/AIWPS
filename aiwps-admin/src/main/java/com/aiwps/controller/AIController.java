package com.aiwps.controller;

import com.aiwps.entity.Question;
import com.aiwps.entity.VariantQuestion;
import com.aiwps.entity.WrongQuestion;
import com.aiwps.mapper.QuestionMapper;
import com.aiwps.mapper.VariantQuestionMapper;
import com.aiwps.mapper.WrongQuestionMapper;
import com.aiwps.service.VariantQuestionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private WrongQuestionMapper wrongQuestionMapper;
    
    @Autowired
    private VariantQuestionMapper variantQuestionMapper;

    /**
     * AI生成变形题
     * 根据错题生成同知识点的变形题
     */
    @PostMapping("/generate-variant")
    public Map<String, Object> generateVariant(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long wrongQuestionId = Long.valueOf(params.get("wrongQuestionId").toString());
            Long studentId = Long.valueOf(params.get("studentId").toString());
            
            // 获取错题信息
            WrongQuestion wrongQuestion = wrongQuestionMapper.selectById(wrongQuestionId);
            if (wrongQuestion == null) {
                result.put("code", 400);
                result.put("message", "错题不存在");
                return result;
            }
            
            // 获取原题
            Question originalQuestion = questionMapper.selectById(wrongQuestion.getQuestionId());
            if (originalQuestion == null) {
                result.put("code", 400);
                result.put("message", "原题不存在");
                return result;
            }
            
            // 生成变形题（这里先用简单的数值变换模拟，实际应该调用AI接口）
            Question variantQuestion = generateVariantQuestion(originalQuestion);
            
            // 保存变形题
            questionMapper.insert(variantQuestion);
            
            // 记录变形题关系
            VariantQuestion variantRecord = new VariantQuestion();
            variantRecord.setOriginalQuestionId(originalQuestion.getId());
            variantRecord.setVariantQuestionId(variantQuestion.getId());
            variantRecord.setStudentId(studentId);
            variantRecord.setVariantType("NUMBER_CHANGE");
            variantRecord.setGeneratedBy("AI");
            variantRecord.setStatus(1); // 直接通过审核
            variantRecord.setCreatedAt(LocalDateTime.now());
            variantQuestionMapper.insert(variantRecord);
            
            result.put("code", 200);
            result.put("message", "生成成功");
            result.put("data", Map.of(
                "variantQuestion", variantQuestion,
                "originalQuestion", originalQuestion
            ));
            
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "生成失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 批量生成变形题
     */
    @PostMapping("/generate-variant-batch")
    public Map<String, Object> generateVariantBatch(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Long> wrongQuestionIds = (List<Long>) params.get("wrongQuestionIds");
            Long studentId = Long.valueOf(params.get("studentId").toString());
            
            List<Map<String, Object>> variants = new ArrayList<>();
            
            for (Long wrongId : wrongQuestionIds) {
                Map<String, Object> singleParams = new HashMap<>();
                singleParams.put("wrongQuestionId", wrongId);
                singleParams.put("studentId", studentId);
                
                Map<String, Object> singleResult = generateVariant(singleParams);
                if ("200".equals(String.valueOf(singleResult.get("code")))) {
                    variants.add((Map<String, Object>) singleResult.get("data"));
                }
            }
            
            result.put("code", 200);
            result.put("message", "批量生成完成");
            result.put("data", variants);
            
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "批量生成失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取变形题列表
     */
    @GetMapping("/variant-list")
    public Map<String, Object> getVariantList(@RequestParam Long studentId) {
        Map<String, Object> result = new HashMap<>();
        
        LambdaQueryWrapper<VariantQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VariantQuestion::getStudentId, studentId);
        wrapper.eq(VariantQuestion::getStatus, 1); // 已审核通过
        wrapper.orderByDesc(VariantQuestion::getCreatedAt);
        
        List<VariantQuestion> variants = variantQuestionMapper.selectList(wrapper);
        
        // 获取详情
        List<Map<String, Object>> detailList = new ArrayList<>();
        for (VariantQuestion vq : variants) {
            Question original = questionMapper.selectById(vq.getOriginalQuestionId());
            Question variant = questionMapper.selectById(vq.getVariantQuestionId());
            detailList.add(Map.of(
                "id", vq.getId(),
                "originalQuestion", original,
                "variantQuestion", variant,
                "variantType", vq.getVariantType(),
                "createdAt", vq.getCreatedAt()
            ));
        }
        
        result.put("code", 200);
        result.put("data", detailList);
        
        return result;
    }

    /**
     * 审核通过变形题
     */
    @PostMapping("/variant-approve/{id}")
    public Map<String, Object> approveVariant(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        VariantQuestion vq = variantQuestionMapper.selectById(id);
        if (vq == null) {
            result.put("code", 404);
            result.put("message", "变形题不存在");
            return result;
        }
        
        vq.setStatus(1);
        variantQuestionMapper.updateById(vq);
        
        result.put("code", 200);
        result.put("message", "审核通过");
        
        return result;
    }

    /**
     * 简单的变形题生成（实际项目中应该调用AI接口）
     * 这里用数值变换模拟
     */
    private Question generateVariantQuestion(Question original) {
        Question variant = new Question();
        variant.setTenantId(original.getTenantId());
        variant.setSubjectId(original.getSubjectId());
        variant.setGradeId(original.getGradeId());
        variant.setType(original.getType());
        variant.setDifficulty(original.getDifficulty());
        variant.setContent(generateVariantContent(original.getContent()));
        variant.setAnswer(generateVariantAnswer(original.getAnswer(), original.getContent()));
        variant.setAnalysis("这是AI生成的变形题");
        variant.setStatus(1);
        variant.setCreatedAt(LocalDateTime.now());
        
        return variant;
    }

    private String generateVariantContent(String content) {
        if (content == null) return content;
        // 简单的数值变换 - 将数字替换
        return content.replaceAll("\\d+", String.valueOf((int) (Math.random() * 100 + 10)));
    }

    private String generateVariantAnswer(String answer, String content) {
        if (answer == null) return answer;
        // 简单模拟 - 实际应该根据变形后的题目重新计算
        return answer;
    }
}
