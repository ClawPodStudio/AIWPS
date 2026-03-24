package com.aiwps.controller;

import com.aiwps.entity.*;
import com.aiwps.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wrong-question")
public class WrongQuestionController {
    
    @Autowired
    private WrongQuestionService wrongQuestionService;
    
    @Autowired
    private KnowledgePointService knowledgePointService;
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private WrongQuestionSortConfigService sortConfigService;
    
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
    
    /**
     * 获取排序后的错题列表
     * 排序算法：考点频次 × 得分权重 × (1/难度)
     */
    @GetMapping("/sorted-list")
    public Map<String, Object> sortedList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Integer isCorrected) {
        
        // 获取学生的排序配置
        LambdaQueryWrapper<WrongQuestionSortConfig> configWrapper = new LambdaQueryWrapper<>();
        configWrapper.eq(WrongQuestionSortConfig::getStudentId, studentId);
        WrongQuestionSortConfig config = sortConfigService.getOne(configWrapper, false);
        
        // 默认权重
        BigDecimal examFreqWeight = config != null && config.getExamFreqWeight() != null 
            ? config.getExamFreqWeight() : new BigDecimal("1.0");
        BigDecimal scoreWeight = config != null && config.getScoreWeight() != null 
            ? config.getScoreWeight() : new BigDecimal("1.0");
        BigDecimal difficultyWeight = config != null && config.getDifficultyWeight() != null 
            ? config.getDifficultyWeight() : new BigDecimal("1.0");
        
        // 查询错题列表
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) wrapper.eq(WrongQuestion::getStudentId, studentId);
        if (isCorrected != null) wrapper.eq(WrongQuestion::getIsCorrected, isCorrected);
        wrapper.orderByDesc(WrongQuestion::getStatus); // 未掌握优先
        
        List<WrongQuestion> wrongQuestions = wrongQuestionService.list(wrapper);
        
        // 计算排序分数并填充详细信息
        List<Map<String, Object>> sortedList = new ArrayList<>();
        for (WrongQuestion wq : wrongQuestions) {
            Question question = questionService.getById(wq.getQuestionId());
            if (question == null) continue;
            
            // 获取关联的知识点
            String kpIdsStr = question.getKnowledgePointIds();
            List<Long> kpIds = new ArrayList<>();
            if (kpIdsStr != null && !kpIdsStr.isEmpty()) {
                kpIds = Arrays.stream(kpIdsStr.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
            }
            
            // 计算考点频次
            int totalExamFreq = 0;
            BigDecimal totalScoreWeight = BigDecimal.ZERO;
            int totalDifficulty = 0;
            
            for (Long kpId : kpIds) {
                KnowledgePoint kp = knowledgePointService.getById(kpId);
                if (kp != null) {
                    totalExamFreq += kp.getExamFreq() != null ? kp.getExamFreq() : 0;
                    totalScoreWeight = totalScoreWeight.add(kp.getScoreWeight() != null ? kp.getScoreWeight() : BigDecimal.ONE);
                }
            }
            
            // 难度系数：1/难度 (难度1=1.0, 难度2=0.5, 难度3=0.33)
            BigDecimal difficultyFactor = BigDecimal.ONE.divide(
                BigDecimal.valueOf(question.getDifficulty() != null ? question.getDifficulty() : 2),
                4, RoundingMode.HALF_UP);
            
            // 计算综合排序分 = 考点频次 × 考点频次权重 + 得分权重 × 得分权重系数 + 难度系数 × 难度权重
            BigDecimal sortScore = 
                BigDecimal.valueOf(totalExamFreq).multiply(examFreqWeight)
                .add(totalScoreWeight.multiply(scoreWeight))
                .add(difficultyFactor.multiply(difficultyWeight));
            
            wq.setExamFreq(totalExamFreq);
            wq.setSortScore(sortScore);
            
            // 构建返回数据
            Map<String, Object> item = new HashMap<>();
            item.put("id", wq.getId());
            item.put("studentId", wq.getStudentId());
            item.put("questionId", wq.getQuestionId());
            item.put("wrongAnswer", wq.getWrongAnswer());
            item.put("isCorrected", wq.getIsCorrected());
            item.put("wrongCount", wq.getWrongCount());
            item.put("lastWrongTime", wq.getLastWrongTime());
            item.put("status", wq.getStatus());
            item.put("examFreq", totalExamFreq);
            item.put("sortScore", sortScore);
            
            // 附加题目信息
            Map<String, Object> questionInfo = new HashMap<>();
            questionInfo.put("id", question.getId());
            questionInfo.put("type", question.getType());
            questionInfo.put("content", question.getContent());
            questionInfo.put("answer", question.getAnswer());
            questionInfo.put("difficulty", question.getDifficulty());
            questionInfo.put("knowledgePointIds", kpIds);
            item.put("question", questionInfo);
            
            sortedList.add(item);
        }
        
        // 按 sortScore 降序排序
        sortedList.sort((a, b) -> {
            BigDecimal scoreA = (BigDecimal) a.get("sortScore");
            BigDecimal scoreB = (BigDecimal) b.get("sortScore");
            return scoreB.compareTo(scoreA);
        });
        
        // 分页
        int total = sortedList.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Map<String, Object>> pagedList = start < total ? sortedList.subList(start, end) : new ArrayList<>();
        
        return Map.of("code", 200, "msg", "success", "data",
                Map.of("records", pagedList, "total", total,
                       "pages", (total + pageSize - 1) / pageSize, "current", pageNum,
                       "config", Map.of(
                           "examFreqWeight", examFreqWeight,
                           "scoreWeight", scoreWeight,
                           "difficultyWeight", difficultyWeight
                       )));
    }
    
    /**
     * 配置排序规则
     */
    @PostMapping("/config-sort")
    public Map<String, Object> configSort(@RequestBody Map<String, Object> params) {
        Long studentId = params.get("studentId") != null ? Long.valueOf(params.get("studentId").toString()) : null;
        Long tenantId = params.get("tenantId") != null ? Long.valueOf(params.get("tenantId").toString()) : null;
        BigDecimal examFreqWeight = params.get("examFreqWeight") != null 
            ? new BigDecimal(params.get("examFreqWeight").toString()) : new BigDecimal("1.0");
        BigDecimal scoreWeight = params.get("scoreWeight") != null 
            ? new BigDecimal(params.get("scoreWeight").toString()) : new BigDecimal("1.0");
        BigDecimal difficultyWeight = params.get("difficultyWeight") != null 
            ? new BigDecimal(params.get("difficultyWeight").toString()) : new BigDecimal("1.0");
        
        if (studentId == null) {
            return Map.of("code", 400, "msg", "学生ID不能为空");
        }
        
        // 查询现有配置
        LambdaQueryWrapper<WrongQuestionSortConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestionSortConfig::getStudentId, studentId);
        WrongQuestionSortConfig config = sortConfigService.getOne(wrapper, false);
        
        if (config == null) {
            // 创建新配置
            config = new WrongQuestionSortConfig();
            config.setTenantId(tenantId);
            config.setStudentId(studentId);
            config.setExamFreqWeight(examFreqWeight);
            config.setScoreWeight(scoreWeight);
            config.setDifficultyWeight(difficultyWeight);
            config.setStatus(1);
            config.setCreatedAt(java.time.LocalDateTime.now());
            sortConfigService.save(config);
        } else {
            // 更新配置
            config.setExamFreqWeight(examFreqWeight);
            config.setScoreWeight(scoreWeight);
            config.setDifficultyWeight(difficultyWeight);
            config.setUpdatedAt(java.time.LocalDateTime.now());
            sortConfigService.updateById(config);
        }
        
        return Map.of("code", 200, "msg", "配置成功", "data", config);
    }
    
    /**
     * 获取排序配置
     */
    @GetMapping("/config-sort")
    public Map<String, Object> getConfigSort(@RequestParam(required = false) Long studentId) {
        if (studentId == null) {
            // 返回默认配置
            return Map.of("code", 200, "msg", "success", "data", Map.of(
                "examFreqWeight", 1.0,
                "scoreWeight", 1.0,
                "difficultyWeight", 1.0
            ));
        }
        
        LambdaQueryWrapper<WrongQuestionSortConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestionSortConfig::getStudentId, studentId);
        WrongQuestionSortConfig config = sortConfigService.getOne(wrapper, false);
        
        if (config == null) {
            return Map.of("code", 200, "msg", "success", "data", Map.of(
                "examFreqWeight", 1.0,
                "scoreWeight", 1.0,
                "difficultyWeight", 1.0
            ));
        }
        
        return Map.of("code", 200, "msg", "success", "data", Map.of(
            "examFreqWeight", config.getExamFreqWeight(),
            "scoreWeight", config.getScoreWeight(),
            "difficultyWeight", config.getDifficultyWeight(),
            "status", config.getStatus()
        ));
    }
    
    @GetMapping("/stats")
    public Map<String, Object> stats(@RequestParam(required = false) Long studentId) {
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) wrapper.eq(WrongQuestion::getStudentId, studentId);
        
        long totalWrong = wrongQuestionService.count(wrapper);
        
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
