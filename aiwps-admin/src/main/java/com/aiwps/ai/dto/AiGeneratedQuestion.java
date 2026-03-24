package com.aiwps.ai.dto;

import lombok.Data;

@Data
public class AiGeneratedQuestion {
    /**
     * 题目内容
     */
    private String content;
    
    /**
     * 选项A（选择题用）
     */
    private String optionA;
    
    /**
     * 选项B
     */
    private String optionB;
    
    /**
     * 选项C
     */
    private String optionC;
    
    /**
     * 选项D
     */
    private String optionD;
    
    /**
     * 正确答案
     */
    private String answer;
    
    /**
     * 答案解析
     */
    private String analysis;
    
    /**
     * 难度等级 1-5
     */
    private Integer difficulty;
    
    /**
     * 题型
     */
    private String questionType;
    
    /**
     * 所属知识点
     */
    private String knowledgePointNames;
    
    /**
     * 标签
     */
    private String tags;
    
    /**
     * 题目来源（AI生成/AI变形）
     */
    private String source;
}
