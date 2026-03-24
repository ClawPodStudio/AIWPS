package com.aiwps.controller;

import com.aiwps.entity.KnowledgePoint;
import com.aiwps.service.KnowledgePointService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/knowledge-point")
public class KnowledgePointController {
    
    @Autowired
    private KnowledgePointService knowledgePointService;
    
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) String name) {
        
        LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<>();
        if (subjectId != null) wrapper.eq(KnowledgePoint::getSubjectId, subjectId);
        if (gradeId != null) wrapper.eq(KnowledgePoint::getGradeId, gradeId);
        if (parentId != null) wrapper.eq(KnowledgePoint::getParentId, parentId);
        if (name != null && !name.isEmpty()) wrapper.like(KnowledgePoint::getName, name);
        
        wrapper.orderByAsc(KnowledgePoint::getSortOrder);
        
        Page<KnowledgePoint> page = new Page<>(pageNum, pageSize);
        Page<KnowledgePoint> result = knowledgePointService.page(page, wrapper);
        
        return Map.of("code", 200, "msg", "success", "data",
                Map.of("records", result.getRecords(), "total", result.getTotal(),
                       "pages", result.getPages(), "current", result.getCurrent()));
    }
    
    @GetMapping("/tree")
    public Map<String, Object> tree(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long gradeId) {
        
        LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<>();
        if (subjectId != null) wrapper.eq(KnowledgePoint::getSubjectId, subjectId);
        if (gradeId != null) wrapper.eq(KnowledgePoint::getGradeId, gradeId);
        wrapper.orderByAsc(KnowledgePoint::getSortOrder);
        
        List<KnowledgePoint> allPoints = knowledgePointService.list(wrapper);
        
        List<KnowledgePoint> rootPoints = allPoints.stream()
                .filter(p -> p.getParentId() == null || p.getParentId() == 0)
                .collect(Collectors.toList());
        
        List<Map<String, Object>> tree = new ArrayList<>();
        for (KnowledgePoint root : rootPoints) {
            Map<String, Object> node = buildTreeNode(root, allPoints);
            tree.add(node);
        }
        
        return Map.of("code", 200, "msg", "success", "data", tree);
    }
    
    private Map<String, Object> buildTreeNode(KnowledgePoint point, List<KnowledgePoint> allPoints) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", point.getId());
        node.put("name", point.getName());
        node.put("subjectId", point.getSubjectId());
        node.put("gradeId", point.getGradeId());
        node.put("parentId", point.getParentId());
        node.put("level", point.getLevel());
        
        List<KnowledgePoint> children = allPoints.stream()
                .filter(p -> Objects.equals(p.getParentId(), point.getId()))
                .collect(Collectors.toList());
        
        if (!children.isEmpty()) {
            List<Map<String, Object>> childNodes = new ArrayList<>();
            for (KnowledgePoint child : children) {
                childNodes.add(buildTreeNode(child, allPoints));
            }
            node.put("children", childNodes);
        }
        
        return node;
    }
    
    @PostMapping
    public Map<String, Object> add(@RequestBody KnowledgePoint knowledgePoint) {
        boolean success = knowledgePointService.save(knowledgePoint);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", knowledgePoint);
        }
        return Map.of("code", 400, "msg", "添加失败");
    }
    
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody KnowledgePoint knowledgePoint) {
        knowledgePoint.setId(id);
        boolean success = knowledgePointService.updateById(knowledgePoint);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", knowledgePoint);
        }
        return Map.of("code", 400, "msg", "更新失败");
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        boolean success = knowledgePointService.removeById(id);
        if (success) {
            return Map.of("code", 200, "msg", "删除成功");
        }
        return Map.of("code", 400, "msg", "删除失败");
    }
}
