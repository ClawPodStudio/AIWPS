package com.aiwps.controller;

import com.aiwps.entity.*;
import com.aiwps.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private WrongQuestionService wrongQuestionService;
    
    @Autowired
    private StudyRecordService studyRecordService;
    
    @Autowired
    private KnowledgePointService knowledgePointService;
    
    @Autowired
    private PaperService paperService;
    
    @GetMapping("/global")
    public Map<String, Object> global() {
        long totalQuestions = questionService.count();
        long totalPapers = paperService.count();
        long totalWrongQuestions = wrongQuestionService.count();
        long totalKnowledgePoints = knowledgePointService.count();
        
        Map<String, Object> data = new HashMap<>();
        data.put("totalQuestions", totalQuestions);
        data.put("totalPapers", totalPapers);
        data.put("totalWrongQuestions", totalWrongQuestions);
        data.put("totalKnowledgePoints", totalKnowledgePoints);
        data.put("totalStudents", 0);
        data.put("totalTeachers", 0);
        
        return Map.of("code", 200, "msg", "success", "data", data);
    }
    
    @GetMapping("/practice-trend")
    public Map<String, Object> practiceTrend(@RequestParam(defaultValue = "30") Integer days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("practiceCount", 0);
            dayData.put("correctCount", 0);
            dayData.put("wrongCount", 0);
            trend.add(dayData);
        }
        
        return Map.of("code", 200, "msg", "success", "data", trend);
    }
    
    @GetMapping("/subject")
    public Map<String, Object> subject() {
        List<Map<String, Object>> subjectStats = new ArrayList<>();
        
        // Get all subjects - this would need SubjectService
        // For now return empty stats
        Map<String, Object> data = new HashMap<>();
        data.put("subjectStats", subjectStats);
        
        return Map.of("code", 200, "msg", "success", "data", data);
    }
}
