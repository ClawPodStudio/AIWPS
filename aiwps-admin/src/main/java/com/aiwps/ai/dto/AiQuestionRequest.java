package com.aiwps.ai.dto;

import lombok.Data;
import java.util.List;

@Data
public class AiQuestionRequest {
    /**
     * 租户ID
     */
    private Long tenantId;
    
    /**
     * 学科ID
     */
    private Long subjectId;
    
    /**
     * 年级ID
     */
    private Long gradeId;
    
    /**
     * 知识点ID列表（用于关联出题）
     */
    private List<Long> knowledgePointIds;
    
    /**
     * 题型：choice(选择题), fill_blank(填空题), essay(简答题), calculation(计算题)
     */
    private String questionType;
    
    /**
     * 难度：1-5
     */
    private Integer difficulty;
    
    /**
     * 出题数量
     */
    private Integer count;
    
    /**
     * 基于的题目ID（用于变形题生成）
     */
    private Long baseQuestionId;
    
    /**
     * 变形类型：数字变化, 条件变化, 逆向, 拓展, 综合
     */
    private String transformType;
    
    /**
     * 学生ID（用于艾宾浩斯记忆曲线）
     */
    private Long studentId;
    
    /**
     * 是否启用艾宾浩斯记忆曲线
     */
    private Boolean enableEbbinghaus;
}
