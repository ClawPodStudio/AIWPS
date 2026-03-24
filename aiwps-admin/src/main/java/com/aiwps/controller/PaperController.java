package com.aiwps.controller;

import com.aiwps.entity.Paper;
import com.aiwps.entity.PaperQuestion;
import com.aiwps.entity.Question;
import com.aiwps.service.PaperService;
import com.aiwps.service.PaperQuestionService;
import com.aiwps.service.QuestionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/paper")
public class PaperController {
    
    @Autowired
    private PaperService paperService;
    
    @Autowired
    private PaperQuestionService paperQuestionService;
    
    @Autowired
    private QuestionService questionService;
    
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) String paperType,
            @RequestParam(required = false) String title) {
        
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) wrapper.eq(Paper::getTenantId, tenantId);
        if (subjectId != null) wrapper.eq(Paper::getSubjectId, subjectId);
        if (gradeId != null) wrapper.eq(Paper::getGradeId, gradeId);
        if (paperType != null && !paperType.isEmpty()) wrapper.eq(Paper::getPaperType, paperType);
        if (title != null && !title.isEmpty()) wrapper.like(Paper::getTitle, title);
        
        wrapper.orderByDesc(Paper::getCreatedAt);
        
        Page<Paper> page = new Page<>(pageNum, pageSize);
        Page<Paper> result = paperService.page(page, wrapper);
        
        return Map.of("code", 200, "msg", "success", "data",
                Map.of("records", result.getRecords(), "total", result.getTotal(),
                       "pages", result.getPages(), "current", result.getCurrent()));
    }
    
    @PostMapping
    public Map<String, Object> add(@RequestBody Map<String, Object> request) {
        Paper paper = new Paper();
        paper.setTitle((String) (request.get("title") != null ? request.get("title") : request.get("name")));
        paper.setSubjectId(request.get("subjectId") != null ? ((Number) request.get("subjectId")).longValue() : null);
        paper.setGradeId(request.get("gradeId") != null ? ((Number) request.get("gradeId")).longValue() : null);
        paper.setTenantId(request.get("tenantId") != null ? ((Number) request.get("tenantId")).longValue() : 1L);
        paper.setTeacherId(1L);
        paper.setTotalScore(request.get("totalScore") != null ? ((Number) request.get("totalScore")).intValue() : 100);
        paper.setPaperType((String) request.get("paperType"));
        paper.setStatus(1);
        paper.setCreatedAt(LocalDateTime.now());
        paper.setUpdatedAt(LocalDateTime.now());
        
        boolean success = paperService.save(paper);
        if (success) {
            Object questionIdsObj = request.get("questionIds");
            if (questionIdsObj instanceof List) {
                List<Number> questionIds = (List<Number>) questionIdsObj;
                int order = 1;
                for (Number qid : questionIds) {
                    PaperQuestion pq = new PaperQuestion();
                    pq.setPaperId(paper.getId());
                    pq.setQuestionId(qid.longValue());
                    pq.setQuestionOrder(order++);
                    pq.setScore(5);
                    pq.setCreatedAt(LocalDateTime.now());
                    paperQuestionService.save(pq);
                }
                paper.setQuestionCount(questionIds.size());
                paperService.updateById(paper);
            }
            return Map.of("code", 200, "msg", "success", "data", paper);
        }
        return Map.of("code", 400, "msg", "添加失败");
    }
    
    @GetMapping("/question")
    public Map<String, Object> getPaperQuestions(@RequestParam Long paperId) {
        // Get paper questions sorted by sort_order
        List<PaperQuestion> pqs = paperQuestionService.list(
            new LambdaQueryWrapper<PaperQuestion>()
                .eq(PaperQuestion::getPaperId, paperId)
                .orderByAsc(PaperQuestion::getQuestionOrder)
        );
        
        if (pqs.isEmpty()) {
            return Map.of("code", 200, "msg", "success", "data", new ArrayList<>());
        }
        
        List<Long> questionIds = pqs.stream()
                .map(PaperQuestion::getQuestionId)
                .collect(Collectors.toList());
        
        List<Question> questions = questionService.list(
                new LambdaQueryWrapper<Question>().in(Question::getId, questionIds)
        );
        
        return Map.of("code", 200, "msg", "success", "data", questions);
    }
    
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Paper paper) {
        paper.setId(id);
        paper.setUpdatedAt(LocalDateTime.now());
        boolean success = paperService.updateById(paper);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", paper);
        }
        return Map.of("code", 400, "msg", "更新失败");
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        paperQuestionService.remove(new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, id));
        boolean success = paperService.removeById(id);
        if (success) {
            return Map.of("code", 200, "msg", "删除成功");
        }
        return Map.of("code", 400, "msg", "删除失败");
    }
}
