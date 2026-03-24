package com.aiwps.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 艾宾浩斯记忆曲线服务
 * 
 * 记忆阶段：
 * - 初始学习 (initial)
 * - 第1次复习 - 1天后 (review_1)
 * - 第2次复习 - 3天后 (review_2) 
 * - 第3次复习 - 7天后 (review_3)
 * - 第4次复习 - 14天后 (review_4)
 * - 第5次复习 - 30天后 (review_5)
 * - 长期记忆 (long_term)
 */
@Slf4j
@Service
public class EbbinghausService {
    
    /**
     * 艾宾浩斯记忆曲线复习间隔（天）
     */
    private static final int[] REVIEW_INTERVALS = {1, 3, 7, 14, 30};
    
    /**
     * 根据答题结果计算下次复习时间
     * 
     * @param currentReviewCount 当前复习次数
     * @param isCorrect 答题是否正确
     * @return 下次复习时间
     */
    public LocalDateTime calculateNextReviewTime(int currentReviewCount, boolean isCorrect) {
        LocalDateTime now = LocalDateTime.now();
        
        if (!isCorrect) {
            // 答错则重新开始记忆周期
            log.info("答题错误，重新开始记忆周期");
            return now.plusDays(1);
        }
        
        // 根据当前复习次数确定下次间隔
        int nextInterval;
        if (currentReviewCount >= REVIEW_INTERVALS.length) {
            // 已完成所有复习间隔，进入长期记忆
            nextInterval = REVIEW_INTERVALS[REVIEW_INTERVALS.length - 1];
        } else {
            nextInterval = REVIEW_INTERVALS[currentReviewCount];
        }
        
        return now.plusDays(nextInterval);
    }
    
    /**
     * 获取记忆阶段名称
     * 
     * @param reviewCount 复习次数
     * @return 记忆阶段
     */
    public String getMemoryStage(int reviewCount) {
        if (reviewCount == 0) {
            return "initial";
        } else if (reviewCount >= REVIEW_INTERVALS.length) {
            return "long_term";
        } else {
            return "review_" + reviewCount;
        }
    }
    
    /**
     * 计算知识点掌握度
     * 
     * @param studyRecords 学习记录
     * @return 掌握度 0-100
     */
    public int calculateMasteryLevel(Map<String, Object> studyRecords) {
        // 简化实现：基于正确率和复习次数计算
        Object correctCountObj = studyRecords.get("correctCount");
        Object totalCountObj = studyRecords.get("totalCount");
        Object reviewCountObj = studyRecords.get("reviewCount");
        
        if (correctCountObj == null || totalCountObj == null) {
            return 0;
        }
        
        int correctCount = ((Number) correctCountObj).intValue();
        int totalCount = ((Number) totalCountObj).intValue();
        int reviewCount = reviewCountObj != null ? ((Number) reviewCountObj).intValue() : 0;
        
        if (totalCount == 0) {
            return 0;
        }
        
        // 正确率权重70%，复习次数权重30%
        double accuracyRate = (double) correctCount / totalCount;
        double masteryLevel = accuracyRate * 0.7 + Math.min(reviewCount * 0.1, 0.3);
        
        return (int) (masteryLevel * 100);
    }
    
    /**
     * 获取需要复习的知识点
     * 
     * @param studyRecords 学习记录
     * @return 需要复习的知识点ID列表
     */
    public Map<Long, LocalDateTime> getReviewableKnowledgePoints(Map<Long, Map<String, Object>> studyRecords) {
        Map<Long, LocalDateTime> reviewable = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (Map.Entry<Long, Map<String, Object>> entry : studyRecords.entrySet()) {
            Map<String, Object> record = entry.getValue();
            Object nextReviewAtObj = record.get("nextReviewAt");
            
            if (nextReviewAtObj != null) {
                LocalDateTime nextReviewAt = (LocalDateTime) nextReviewAtObj;
                if (nextReviewAt.isBefore(now) || nextReviewAt.isEqual(now)) {
                    reviewable.put(entry.getKey(), nextReviewAt);
                }
            }
        }
        
        return reviewable;
    }
    
    /**
     * 生成个性化复习建议
     * 
     * @param masteryLevel 掌握度
     * @return 复习建议
     */
    public String getReviewSuggestion(int masteryLevel) {
        if (masteryLevel >= 90) {
            return "知识点掌握牢固，保持每周复习一次即可";
        } else if (masteryLevel >= 70) {
            return "知识点基本掌握，建议每3天复习一次";
        } else if (masteryLevel >= 50) {
            return "知识点掌握一般，需要加强复习，建议每天复习";
        } else {
            return "知识点掌握较差，需要重点突破，建议多次重复学习";
        }
    }
}
