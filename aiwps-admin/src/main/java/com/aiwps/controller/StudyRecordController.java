package com.aiwps.controller;

import com.aiwps.entity.StudyRecord;
import com.aiwps.service.StudyRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/study-record")
public class StudyRecordController {
    
    @Autowired
    private StudyRecordService studyRecordService;
    
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long questionId) {
        
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) wrapper.eq(StudyRecord::getStudentId, studentId);
        if (questionId != null) wrapper.eq(StudyRecord::getQuestionId, questionId);
        
        wrapper.orderByDesc(StudyRecord::getCreatedAt);
        
        Page<StudyRecord> page = new Page<>(pageNum, pageSize);
        Page<StudyRecord> result = studyRecordService.page(page, wrapper);
        
        return Map.of("code", 200, "msg", "success", "data",
                Map.of("records", result.getRecords(), "total", result.getTotal(),
                       "pages", result.getPages(), "current", result.getCurrent()));
    }
    
    @PostMapping
    public Map<String, Object> add(@RequestBody StudyRecord studyRecord) {
        boolean success = studyRecordService.save(studyRecord);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", studyRecord);
        }
        return Map.of("code", 400, "msg", "添加失败");
    }
}
