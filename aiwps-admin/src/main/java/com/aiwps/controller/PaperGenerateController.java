package com.aiwps.controller;

import com.aiwps.entity.Paper;
import com.aiwps.entity.PaperQuestion;
import com.aiwps.entity.Question;
import com.aiwps.mapper.PaperMapper;
import com.aiwps.mapper.PaperQuestionMapper;
import com.aiwps.mapper.QuestionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/paper")
public class PaperGenerateController {

    @Autowired
    private PaperMapper paperMapper;
    
    @Autowired
    private PaperQuestionMapper paperQuestionMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
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
            int totalQuestions = questionTypes.values().stream().mapToInt(Integer::intValue).sum();
            int scorePerQuestion = totalScore / (totalQuestions > 0 ? totalQuestions : 1);
            
            for (Map.Entry<String, Integer> entry : questionTypes.entrySet()) {
                String type = entry.getKey();
                int count = entry.getValue();
                
                // 查询该题型的题目
                LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Question::getSubjectId, subjectId);
                if (gradeId != null) {
                    wrapper.eq(Question::getGradeId, gradeId);
                }
                wrapper.eq(Question::getType, type);
                wrapper.eq(Question::getStatus, 1);
                wrapper.last("LIMIT " + count * 2);
                
                List<Question> questions = questionMapper.selectList(wrapper);
                
                int added = 0;
                for (Question q : questions) {
                    if (added >= count) break;
                    
                    // 添加到试卷
                    PaperQuestion pq = new PaperQuestion();
                    pq.setPaperId(paper.getId());
                    pq.setQuestionId(q.getId());
                    pq.setScore(scorePerQuestion);
                    pq.setQuestionOrder(paperQuestions.size() + 1);
                    pq.setCreatedAt(LocalDateTime.now());
                    paperQuestionMapper.insert(pq);
                    
                    paperQuestions.add(Map.of(
                        "id", q.getId(),
                        "content", q.getContent(),
                        "answer", q.getAnswer(),
                        "type", q.getType(),
                        "difficulty", q.getDifficulty(),
                        "score", scorePerQuestion
                    ));
                    
                    currentScore += scorePerQuestion;
                    added++;
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
            result.put("code", 500);
            result.put("message", "生成失败: " + e.getMessage());
        }
        
        return result;
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
