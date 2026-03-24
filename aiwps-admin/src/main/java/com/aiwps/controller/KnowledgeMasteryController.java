package com.aiwps.controller;

import com.aiwps.entity.KnowledgeMastery;
import com.aiwps.service.KnowledgeMasteryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/knowledge-mastery")
public class KnowledgeMasteryController {
    
    @Autowired
    private KnowledgeMasteryService knowledgeMasteryService;
    
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long knowledgePointId,
            @RequestParam(required = false) Long subjectId) {
        
        LambdaQueryWrapper<KnowledgeMastery> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) wrapper.eq(KnowledgeMastery::getStudentId, studentId);
        if (knowledgePointId != null) wrapper.eq(KnowledgeMastery::getKnowledgePointId, knowledgePointId);
        
        wrapper.orderByDesc(KnowledgeMastery::getUpdatedAt);
        
        Page<KnowledgeMastery> page = new Page<>(pageNum, pageSize);
        Page<KnowledgeMastery> result = knowledgeMasteryService.page(page, wrapper);
        
        return Map.of("code", 200, "msg", "success", "data",
                Map.of("records", result.getRecords(), "total", result.getTotal(),
                       "pages", result.getPages(), "current", result.getCurrent()));
    }
    
    @PostMapping
    public Map<String, Object> add(@RequestBody KnowledgeMastery knowledgeMastery) {
        boolean success = knowledgeMasteryService.save(knowledgeMastery);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", knowledgeMastery);
        }
        return Map.of("code", 400, "msg", "添加失败");
    }
    
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody KnowledgeMastery knowledgeMastery) {
        knowledgeMastery.setId(id);
        boolean success = knowledgeMasteryService.updateById(knowledgeMastery);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", knowledgeMastery);
        }
        return Map.of("code", 400, "msg", "更新失败");
    }
}
