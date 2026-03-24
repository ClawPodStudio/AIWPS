package com.aiwps.controller;

import com.aiwps.entity.KnowledgeMastery;
import com.aiwps.entity.WrongQuestion;
import com.aiwps.mapper.KnowledgeMasteryMapper;
import com.aiwps.mapper.WrongQuestionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private KnowledgeMasteryMapper knowledgeMasteryMapper;
    
    @Autowired
    private WrongQuestionMapper wrongQuestionMapper;

    // 艾宾浩斯记忆周期（天）
    private static final int[] EBINGHAUS_INTERVALS = {1, 2, 4, 7, 15, 30, 60};

    /**
     * 获取今日复习任务
     */
    @GetMapping("/ebbinghaus/today")
    public Map<String, Object> getTodayReviewTasks(@RequestParam Long studentId) {
        Map<String, Object> result = new HashMap<>();
        
        // 查找今日应该复习的知识点
        LambdaQueryWrapper<KnowledgeMastery> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeMastery::getStudentId, studentId);
        wrapper.le(KnowledgeMastery::getNextReviewTime, LocalDateTime.now());
        wrapper.eq(KnowledgeMastery::getIsRemoved, 0);
        
        List<KnowledgeMastery> reviewTasks = knowledgeMasteryMapper.selectList(wrapper);
        
        List<Map<String, Object>> tasks = new ArrayList<>();
        for (KnowledgeMastery task : reviewTasks) {
            tasks.add(Map.of(
                "id", task.getId(),
                "knowledgePointId", task.getKnowledgePointId(),
                "knowledgePointName", "知识点" + task.getKnowledgePointId(),
                "masteryLevel", task.getMasteryLevel() != null ? task.getMasteryLevel() : 0,
                "nextReviewTime", task.getNextReviewTime(),
                "status", task.getMasteryLevel() != null && task.getMasteryLevel().compareTo(new BigDecimal("80")) >= 0 ? "已掌握" : "待复习"
            ));
        }
        
        result.put("code", 200);
        result.put("data", tasks);
        result.put("total", tasks.size());
        
        return result;
    }

    /**
     * 记录复习结果并计算下次复习时间
     */
    @PostMapping("/ebbinghaus/record")
    public Map<String, Object> recordReviewResult(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long studentId = Long.valueOf(params.get("studentId").toString());
            Long knowledgePointId = Long.valueOf(params.get("knowledgePointId").toString());
            Boolean isCorrect = Boolean.valueOf(params.get("isCorrect").toString());
            
            // 查找或创建掌握度记录
            LambdaQueryWrapper<KnowledgeMastery> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(KnowledgeMastery::getStudentId, studentId)
                   .eq(KnowledgeMastery::getKnowledgePointId, knowledgePointId);
            
            KnowledgeMastery mastery = knowledgeMasteryMapper.selectOne(wrapper);
            
            if (mastery == null) {
                mastery = new KnowledgeMastery();
                mastery.setStudentId(studentId);
                mastery.setKnowledgePointId(knowledgePointId);
                mastery.setCorrectCount(0);
                mastery.setWrongCount(0);
                mastery.setMasteryLevel(BigDecimal.ZERO);
            }
            
            // 更新正确/错误次数
            if (isCorrect) {
                mastery.setCorrectCount(mastery.getCorrectCount() + 1);
            } else {
                mastery.setWrongCount(mastery.getWrongCount() + 1);
            }
            
            // 计算掌握度 (正确率)
            int total = mastery.getCorrectCount() + mastery.getWrongCount();
            if (total > 0) {
                double rate = (double) mastery.getCorrectCount() / total * 100;
                mastery.setMasteryLevel(BigDecimal.valueOf(rate));
            }
            
            // 计算下次复习时间（艾宾浩斯曲线）
            int reviewCount = mastery.getCorrectCount();
            double factor = Math.pow(0.9, mastery.getWrongCount()); // 错题越多，间隔越短
            int intervalIndex = Math.min(reviewCount, EBINGHAUS_INTERVALS.length - 1);
            int intervalDays = (int) (EBINGHAUS_INTERVALS[intervalIndex] * factor);
            
            mastery.setLastReviewTime(LocalDateTime.now());
            mastery.setNextReviewTime(LocalDateTime.now().plusDays(Math.max(1, intervalDays)));
            
            if (mastery.getId() == null) {
                knowledgeMasteryMapper.insert(mastery);
            } else {
                knowledgeMasteryMapper.updateById(mastery);
            }
            
            result.put("code", 200);
            result.put("message", "记录成功");
            result.put("data", Map.of(
                "masteryLevel", mastery.getMasteryLevel(),
                "nextReviewTime", mastery.getNextReviewTime(),
                "intervalDays", Math.max(1, intervalDays)
            ));
            
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "记录失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 计算下次复习时间
     */
    @PostMapping("/calculate")
    public Map<String, Object> calculateNextReview(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int wrongCount = Integer.valueOf(params.get("wrongCount").toString());
            int reviewCount = Integer.valueOf(params.get("reviewCount").toString());
            
            double factor = Math.pow(0.9, wrongCount);
            int intervalIndex = Math.min(reviewCount, EBINGHAUS_INTERVALS.length - 1);
            int intervalDays = (int) (EBINGHAUS_INTERVALS[intervalIndex] * factor);
            
            LocalDateTime nextReview = LocalDateTime.now().plusDays(Math.max(1, intervalDays));
            
            result.put("code", 200);
            result.put("data", Map.of(
                "intervalDays", intervalDays,
                "nextReviewTime", nextReview.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "factor", factor
            ));
            
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "计算失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取复习日历
     */
    @GetMapping("/calendar")
    public Map<String, Object> getReviewCalendar(@RequestParam Long studentId, 
                                                  @RequestParam(required = false) String startDate,
                                                  @RequestParam(required = false) String endDate) {
        Map<String, Object> result = new HashMap<>();
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now();
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now().plusDays(30);
        
        LambdaQueryWrapper<KnowledgeMastery> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeMastery::getStudentId, studentId)
               .le(KnowledgeMastery::getNextReviewTime, end.atStartOfDay())
               .ge(KnowledgeMastery::getNextReviewTime, start.atStartOfDay())
               .eq(KnowledgeMastery::getIsRemoved, 0);
        
        List<KnowledgeMastery> tasks = knowledgeMasteryMapper.selectList(wrapper);
        
        Map<String, List<Map<String, Object>>> calendar = new HashMap<>();
        for (KnowledgeMastery task : tasks) {
            String dateKey = task.getNextReviewTime().toLocalDate().toString();
            calendar.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(Map.of(
                "knowledgePointId", task.getKnowledgePointId(),
                "masteryLevel", task.getMasteryLevel()
            ));
        }
        
        result.put("code", 200);
        result.put("data", calendar);
        
        return result;
    }
}
