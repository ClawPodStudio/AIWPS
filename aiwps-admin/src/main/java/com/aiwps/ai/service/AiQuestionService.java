package com.aiwps.ai.service;

import com.aiwps.ai.dto.AiGeneratedQuestion;
import com.aiwps.ai.dto.AiQuestionRequest;
import com.aiwps.entity.Question;
import com.aiwps.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiQuestionService {
    
    @Autowired
    private LlmService llmService;
    
    @Autowired
    private EbbinghausService ebbinghausService;
    
    @Autowired
    private QuestionService questionService;
    
    /**
     * 智能出题
     * 
     * @param request 出题请求
     * @return 生成的题目列表
     */
    public List<Question> generateQuestions(AiQuestionRequest request) {
        log.info("开始AI智能出题: subjectId={}, gradeId={}, count={}", 
                request.getSubjectId(), request.getGradeId(), request.getCount());
        
        List<AiGeneratedQuestion> generatedQuestions;
        
        // 判断是生成新题还是变形题
        if (request.getBaseQuestionId() != null) {
            // 变形题生成
            Question baseQuestion = questionService.getById(request.getBaseQuestionId());
            if (baseQuestion == null) {
                throw new RuntimeException("基题不存在: " + request.getBaseQuestionId());
            }
            generatedQuestions = llmService.generateTransformQuestions(request, baseQuestion.getContent());
        } else {
            // 知识点关联出题
            generatedQuestions = llmService.generateQuestions(request);
        }
        
        // 转换并保存到数据库
        List<Question> savedQuestions = new ArrayList<>();
        for (AiGeneratedQuestion gq : generatedQuestions) {
            Question question = convertToQuestion(gq, request);
            question.setStatus(1); // 待审核状态
            question.setCreatedAt(LocalDateTime.now());
            question.setUpdatedAt(LocalDateTime.now());
            questionService.save(question);
            savedQuestions.add(question);
        }
        
        log.info("AI智能出题完成，生成题目数: {}", savedQuestions.size());
        return savedQuestions;
    }
    
    /**
     * 基于艾宾浩斯记忆曲线出题
     * 
     * @param studentId 学生ID
     * @param subjectId 学科ID
     * @param count 出题数量
     * @return 题目列表
     */
    public List<Question> generateQuestionsWithEbbinghaus(Long studentId, Long subjectId, Integer count) {
        log.info("基于艾宾浩斯记忆曲线出题: studentId={}, subjectId={}, count={}", 
                studentId, subjectId, count);
        
        // TODO: 从学习记录中获取需要复习的知识点
        // 这里需要注入StudyRecordService来查询学生的学习记录
        Map<Long, Map<String, Object>> studyRecords = new HashMap<>();
        
        // 获取需要复习的知识点
        Map<Long, LocalDateTime> reviewablePoints = ebbinghausService.getReviewableKnowledgePoints(studyRecords);
        
        if (reviewablePoints.isEmpty()) {
            // 没有需要复习的知识点，生成新题
            AiQuestionRequest request = new AiQuestionRequest();
            request.setSubjectId(subjectId);
            request.setCount(count);
            request.setStudentId(studentId);
            request.setEnableEbbinghaus(true);
            return generateQuestions(request);
        }
        
        // TODO: 根据复习知识点生成相应的题目
        // 实际实现需要查询知识点关联的题目
        
        return new ArrayList<>();
    }
    
    /**
     * 知识点关联出题
     * 
     * @param knowledgePointIds 知识点ID列表
     * @param count 出题数量
     * @return 题目列表
     */
    public List<Question> generateQuestionsByKnowledgePoints(List<Long> knowledgePointIds, Integer count) {
        if (knowledgePointIds == null || knowledgePointIds.isEmpty()) {
            throw new RuntimeException("知识点ID不能为空");
        }
        
        AiQuestionRequest request = new AiQuestionRequest();
        request.setKnowledgePointIds(knowledgePointIds);
        request.setCount(count);
        
        return generateQuestions(request);
    }
    
    /**
     * 生成变形题
     * 
     * @param baseQuestionId 基题ID
     * @param transformType 变形类型
     * @param count 变形题数量
     * @return 题目列表
     */
    public List<Question> generateTransformQuestions(Long baseQuestionId, String transformType, Integer count) {
        if (baseQuestionId == null) {
            throw new RuntimeException("基题ID不能为空");
        }
        
        Question baseQuestion = questionService.getById(baseQuestionId);
        if (baseQuestion == null) {
            throw new RuntimeException("基题不存在: " + baseQuestionId);
        }
        
        AiQuestionRequest request = new AiQuestionRequest();
        request.setBaseQuestionId(baseQuestionId);
        request.setTransformType(transformType);
        request.setCount(count);
        
        List<Question> questions = generateQuestions(request);
        
        // 更新题目来源
        for (Question q : questions) {
            q.setSource("AI变形-" + transformType);
        }
        
        return questions;
    }
    
    /**
     * 批量生成试卷
     * 
     * @param request 出题请求
     * @return 试卷题目列表
     */
    public List<Question> generateExamPaper(AiQuestionRequest request) {
        log.info("生成试卷: subjectId={}, gradeId={}, count={}", 
                request.getSubjectId(), request.getGradeId(), request.getCount());
        
        // 根据难度分布生成题目
        List<Question> allQuestions = new ArrayList<>();
        
        // 简单题 20%
        AiQuestionRequest easyRequest = new AiQuestionRequest();
        easyRequest.setSubjectId(request.getSubjectId());
        easyRequest.setGradeId(request.getGradeId());
        easyRequest.setDifficulty(1);
        easyRequest.setQuestionType(request.getQuestionType());
        easyRequest.setKnowledgePointIds(request.getKnowledgePointIds());
        easyRequest.setCount((int) Math.ceil(request.getCount() * 0.2));
        allQuestions.addAll(generateQuestions(easyRequest));
        
        // 中等题 50%
        AiQuestionRequest mediumRequest = new AiQuestionRequest();
        mediumRequest.setSubjectId(request.getSubjectId());
        mediumRequest.setGradeId(request.getGradeId());
        mediumRequest.setDifficulty(3);
        mediumRequest.setQuestionType(request.getQuestionType());
        mediumRequest.setKnowledgePointIds(request.getKnowledgePointIds());
        mediumRequest.setCount((int) Math.ceil(request.getCount() * 0.5));
        allQuestions.addAll(generateQuestions(mediumRequest));
        
        // 困难题 30%
        AiQuestionRequest hardRequest = new AiQuestionRequest();
        hardRequest.setSubjectId(request.getSubjectId());
        hardRequest.setGradeId(request.getGradeId());
        hardRequest.setDifficulty(5);
        hardRequest.setQuestionType(request.getQuestionType());
        hardRequest.setKnowledgePointIds(request.getKnowledgePointIds());
        hardRequest.setCount((int) Math.ceil(request.getCount() * 0.3));
        allQuestions.addAll(generateQuestions(hardRequest));
        
        return allQuestions;
    }
    
    /**
     * 转换AI生成的题目为Question实体
     */
    private Question convertToQuestion(AiGeneratedQuestion gq, AiQuestionRequest request) {
        Question question = new Question();
        question.setTenantId(request.getTenantId());
        question.setSubjectId(request.getSubjectId());
        question.setGradeId(request.getGradeId());
        question.setContent(gq.getContent());
        question.setOptionA(gq.getOptionA());
        question.setOptionB(gq.getOptionB());
        question.setOptionC(gq.getOptionC());
        question.setOptionD(gq.getOptionD());
        question.setAnswer(gq.getAnswer());
        question.setAnalysis(gq.getAnalysis());
        question.setDifficulty(gq.getDifficulty());
        question.setType(gq.getQuestionType());
        question.setTags(gq.getTags());
        question.setKnowledgePointIds(request.getKnowledgePointIds() != null ? 
                String.join(",", request.getKnowledgePointIds().stream()
                        .map(String::valueOf).toList()) : null);
        
        return question;
    }
}
