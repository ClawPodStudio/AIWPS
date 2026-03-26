package com.aiwps.controller;

import com.aiwps.ai.dto.AiQuestionRequest;
import com.aiwps.ai.service.AiQuestionService;
import com.aiwps.entity.Paper;
import com.aiwps.entity.PaperQuestion;
import com.aiwps.entity.Question;
import com.aiwps.mapper.PaperMapper;
import com.aiwps.mapper.PaperQuestionMapper;
import com.aiwps.mapper.QuestionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/paper")
public class PaperGenerateController {

    @Autowired
    private PaperMapper paperMapper;
    
    @Autowired
    private PaperQuestionMapper paperQuestionMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private AiQuestionService aiQuestionService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * AI生成试卷
     */
    @PostMapping("/ai-generate")
    public Map<String, Object> aiGeneratePaper(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long subjectId = Long.valueOf(params.get("subjectId").toString());
            Long gradeId = params.get("gradeId") != null ? Long.valueOf(params.get("gradeId").toString()) : null;
            Long teacherId = params.get("teacherId") != null ? Long.valueOf(params.get("teacherId").toString()) : 1L;
            
            @SuppressWarnings("unchecked")
            List<Long> knowledgePointIds = (List<Long>) params.get("knowledgePointIds");
            Map<String, Integer> questionTypes = objectMapper.convertValue(params.get("questionTypes"), Map.class);
            Integer totalScore = params.get("totalScore") != null ? Integer.valueOf(params.get("totalScore").toString()) : 100;
            
            // 计算总题目数和每题分值
            int totalQuestions = questionTypes.values().stream().mapToInt(Integer::intValue).sum();
            int scorePerQuestion = totalScore / (totalQuestions > 0 ? totalQuestions : 1);
            
            // 创建试卷
            Paper paper = new Paper();
            paper.setTenantId(1L);
            paper.setTeacherId(teacherId);
            paper.setTitle("AI生成试卷-" + System.currentTimeMillis());
            paper.setSubjectId(subjectId);
            paper.setGradeId(gradeId);
            paper.setTotalScore(totalScore);
            paper.setPaperType("AI");
            paper.setStatus(1);
            paper.setCreatedAt(LocalDateTime.now());
            
            paperMapper.insert(paper);
            
            // 按题型生成题目
            List<Map<String, Object>> paperQuestions = new ArrayList<>();
            int currentScore = 0;
            
            for (Map.Entry<String, Integer> entry : questionTypes.entrySet()) {
                String type = entry.getKey();
                int count = entry.getValue();
                
                if (count <= 0) continue;
                
                try {
                    // 使用AI生成题目
                    AiQuestionRequest request = new AiQuestionRequest();
                    request.setSubjectId(subjectId);
                    request.setGradeId(gradeId);
                    request.setKnowledgePointIds(knowledgePointIds);
                    request.setQuestionType(mapQuestionType(type));
                    request.setDifficulty(calculateDifficulty(type));
                    request.setCount(count);
                    request.setTenantId(1L);
                    
                    List<Question> aiQuestions = aiQuestionService.generateQuestions(request);
                    
                    // 如果AI生成失败或数量不足，从题库补充
                    int needed = count - aiQuestions.size();
                    List<Question> dbQuestions = new ArrayList<>();
                    if (needed > 0) {
                        dbQuestions = getQuestionsFromDb(subjectId, gradeId, type, needed);
                    }
                    
                    // 合并AI生成和数据库题目
                    List<Question> allQuestions = new ArrayList<>();
                    allQuestions.addAll(aiQuestions);
                    allQuestions.addAll(dbQuestions);
                    
                    int added = 0;
                    for (Question q : allQuestions) {
                        if (added >= count) break;
                        
                        // 保存题目
                        if (q.getId() == null) {
                            q.setTenantId(1L);
                            q.setStatus(1);
                            q.setCreatedAt(LocalDateTime.now());
                            questionMapper.insert(q);
                        }
                        
                        // 添加到试卷
                        PaperQuestion pq = new PaperQuestion();
                        pq.setPaperId(paper.getId());
                        pq.setQuestionId(q.getId());
                        pq.setScore(scorePerQuestion);
                        pq.setQuestionOrder(paperQuestions.size() + 1);
                        pq.setCreatedAt(LocalDateTime.now());
                        paperQuestionMapper.insert(pq);
                        
                        paperQuestions.add(new HashMap<>() {{
                            put("id", q.getId());
                            put("content", q.getContent());
                            put("answer", q.getAnswer());
                            put("type", q.getType());
                            put("difficulty", q.getDifficulty() != null ? q.getDifficulty() : 2);
                            put("score", scorePerQuestion);
                            put("optionA", q.getOptionA() != null ? q.getOptionA() : "");
                            put("optionB", q.getOptionB() != null ? q.getOptionB() : "");
                            put("optionC", q.getOptionC() != null ? q.getOptionC() : "");
                            put("optionD", q.getOptionD() != null ? q.getOptionD() : "");
                            put("analysis", q.getAnalysis() != null ? q.getAnalysis() : "");
                        }});
                        
                        currentScore += scorePerQuestion;
                        added++;
                    }
                    
                    log.info("AI生成{}型题目{}道（AI:{}, 题库:{}）", type, added, aiQuestions.size(), dbQuestions.size());
                    
                } catch (Exception e) {
                    log.error("生成{}型题目失败: {}", type, e.getMessage());
                    // 发生错误时从题库补充
                    List<Question> dbQuestions = getQuestionsFromDb(subjectId, gradeId, type, count);
                    for (Question q : dbQuestions) {
                        PaperQuestion pq = new PaperQuestion();
                        pq.setPaperId(paper.getId());
                        pq.setQuestionId(q.getId());
                        pq.setScore(scorePerQuestion);
                        pq.setQuestionOrder(paperQuestions.size() + 1);
                        pq.setCreatedAt(LocalDateTime.now());
                        paperQuestionMapper.insert(pq);
                        
                        paperQuestions.add(new HashMap<>() {{
                            put("id", q.getId());
                            put("content", q.getContent());
                            put("answer", q.getAnswer());
                            put("type", q.getType());
                            put("difficulty", q.getDifficulty() != null ? q.getDifficulty() : 2);
                            put("score", scorePerQuestion);
                            put("optionA", q.getOptionA() != null ? q.getOptionA() : "");
                            put("optionB", q.getOptionB() != null ? q.getOptionB() : "");
                            put("optionC", q.getOptionC() != null ? q.getOptionC() : "");
                            put("optionD", q.getOptionD() != null ? q.getOptionD() : "");
                            put("analysis", q.getAnalysis() != null ? q.getAnalysis() : "");
                        }});
                        currentScore += scorePerQuestion;
                    }
                }
            }
            
            // 更新试卷题目数量
            paper.setQuestionCount(paperQuestions.size());
            paperMapper.updateById(paper);
            
            result.put("code", 200);
            result.put("message", "生成成功");
            result.put("data", Map.of(
                "paperId", paper.getId(),
                "paperName", paper.getTitle(),
                "totalScore", currentScore,
                "questionCount", paperQuestions.size(),
                "questions", paperQuestions
            ));
            
        } catch (Exception e) {
            log.error("AI生成试卷失败: {}", e.getMessage(), e);
            result.put("code", 500);
            result.put("message", "生成失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 从数据库获取题目
     */
    private List<Question> getQuestionsFromDb(Long subjectId, Long gradeId, String type, int limit) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getSubjectId, subjectId);
        if (gradeId != null) {
            wrapper.eq(Question::getGradeId, gradeId);
        }
        wrapper.eq(Question::getType, type);
        wrapper.eq(Question::getStatus, 1);
        wrapper.last("LIMIT " + limit);
        
        return questionMapper.selectList(wrapper);
    }
    
    /**
     * 映射题型
     */
    private String mapQuestionType(String type) {
        return switch (type) {
            case "CHOICE" -> "choice";
            case "BLANK" -> "fill_blank";
            case "ANSWER" -> "essay";
            default -> type.toLowerCase();
        };
    }
    
    /**
     * 根据题型计算难度
     */
    private Integer calculateDifficulty(String type) {
        // 选择题难度低一些，解答题难度高一些
        return switch (type) {
            case "CHOICE" -> 2;
            case "BLANK" -> 2;
            case "ANSWER" -> 3;
            default -> 2;
        };
    }

    /**
     * 获取知识点可选范围
     */
    @GetMapping("/knowledge-range")
    public Map<String, Object> getKnowledgeRange(
            @RequestParam Long subjectId,
            @RequestParam(required = false) Long gradeId) {
        Map<String, Object> result = new HashMap<>();
        
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getSubjectId, subjectId);
        if (gradeId != null) {
            wrapper.eq(Question::getGradeId, gradeId);
        }
        wrapper.select(Question::getKnowledgePointIds);
        wrapper.last("LIMIT 100");
        
        List<Question> questions = questionMapper.selectList(wrapper);
        
        // 收集所有知识点
        Set<Long> kpIds = new HashSet<>();
        for (Question q : questions) {
            if (q.getKnowledgePointIds() != null && !q.getKnowledgePointIds().isEmpty()) {
                try {
                    List<?> ids = objectMapper.readValue(q.getKnowledgePointIds(), List.class);
                    ids.forEach(id -> kpIds.add(((Number) id).longValue()));
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        
        result.put("code", 200);
        result.put("data", kpIds);
        
        return result;
    }

    /**
     * 获取试卷模板列表(AI生成的)
     */
    @GetMapping("/templates")
    public Map<String, Object> getTemplates(@RequestParam(required = false) Long subjectId) {
        Map<String, Object> result = new HashMap<>();
        
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Paper::getPaperType, "AI");
        wrapper.eq(Paper::getStatus, 1);
        if (subjectId != null) {
            wrapper.eq(Paper::getSubjectId, subjectId);
        }
        wrapper.orderByDesc(Paper::getCreatedAt);
        wrapper.last("LIMIT 50");
        
        List<Paper> templates = paperMapper.selectList(wrapper);
        
        List<Map<String, Object>> list = new ArrayList<>();
        for (Paper p : templates) {
            list.add(Map.of(
                "id", p.getId(),
                "name", p.getTitle(),
                "subjectId", p.getSubjectId(),
                "gradeId", p.getGradeId(),
                "totalScore", p.getTotalScore(),
                "questionCount", p.getQuestionCount(),
                "createdAt", p.getCreatedAt()
            ));
        }
        
        result.put("code", 200);
        result.put("data", list);
        
        return result;
    }

    /**
     * 获取试卷详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getPaper(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        Paper paper = paperMapper.selectById(id);
        if (paper == null) {
            result.put("code", 404);
            result.put("message", "试卷不存在");
            return result;
        }
        
        // 获取试卷题目
        LambdaQueryWrapper<PaperQuestion> pqWrapper = new LambdaQueryWrapper<>();
        pqWrapper.eq(PaperQuestion::getPaperId, id);
        pqWrapper.orderByAsc(PaperQuestion::getQuestionOrder);
        List<PaperQuestion> pqs = paperQuestionMapper.selectList(pqWrapper);
        
        List<Map<String, Object>> questions = new ArrayList<>();
        for (PaperQuestion pq : pqs) {
            Question q = questionMapper.selectById(pq.getQuestionId());
            if (q != null) {
                questions.add(Map.of(
                    "id", q.getId(),
                    "content", q.getContent(),
                    "answer", q.getAnswer(),
                    "type", q.getType(),
                    "difficulty", q.getDifficulty(),
                    "score", pq.getScore()
                ));
            }
        }
        
        result.put("code", 200);
        result.put("data", Map.of(
            "paper", paper,
            "questions", questions
        ));
        
        return result;
    }
}
