package com.aiwps.controller;

import com.aiwps.entity.KnowledgePoint;
import com.aiwps.entity.KnowledgePointType;
import com.aiwps.service.KnowledgePointService;
import com.aiwps.service.KnowledgePointTypeService;
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
    
    @Autowired
    private KnowledgePointTypeService knowledgePointTypeService;
    
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
    
    /**
     * 按题型获取知识点
     */
    @GetMapping("/type/{type}")
    public Map<String, Object> getByType(@PathVariable String type) {
        // 先通过 knowledge_point_type 表查找关联的知识点ID
        LambdaQueryWrapper<KnowledgePointType> typeWrapper = new LambdaQueryWrapper<>();
        typeWrapper.eq(KnowledgePointType::getQuestionType, type.toUpperCase());
        List<KnowledgePointType> typeList = knowledgePointTypeService.list(typeWrapper);
        
        if (typeList.isEmpty()) {
            // 如果没有绑定表数据，则从 knowledge_point 的 question_types 字段查找
            LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(KnowledgePoint::getQuestionTypes, type.toUpperCase());
            List<KnowledgePoint> points = knowledgePointService.list(wrapper);
            return Map.of("code", 200, "msg", "success", "data", points);
        }
        
        // 获取知识点ID列表
        List<Long> kpIds = typeList.stream()
                .map(KnowledgePointType::getKnowledgePointId)
                .collect(Collectors.toList());
        
        // 批量查询知识点
        List<KnowledgePoint> points = knowledgePointService.listByIds(kpIds);
        return Map.of("code", 200, "msg", "success", "data", points);
    }
    
    /**
     * 绑定知识点与题型
     */
    @PostMapping("/bind-type")
    public Map<String, Object> bindType(@RequestBody Map<String, Object> params) {
        List<Long> knowledgePointIds = (List<Long>) params.get("knowledgePointIds");
        String questionType = (String) params.get("questionType");
        Double weight = params.get("weight") != null ? Double.valueOf(params.get("weight").toString()) : 1.0;
        
        if (knowledgePointIds == null || knowledgePointIds.isEmpty() || questionType == null) {
            return Map.of("code", 400, "msg", "参数不完整");
        }
        
        // 先删除旧的绑定
        LambdaQueryWrapper<KnowledgePointType> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(KnowledgePointType::getQuestionType, questionType.toUpperCase());
        knowledgePointTypeService.remove(deleteWrapper);
        
        // 添加新的绑定
        List<KnowledgePointType> bindings = new ArrayList<>();
        for (Long kpId : knowledgePointIds) {
            KnowledgePointType binding = new KnowledgePointType();
            binding.setKnowledgePointId(kpId);
            binding.setQuestionType(questionType.toUpperCase());
            binding.setWeight(new java.math.BigDecimal(weight));
            binding.setCreatedAt(java.time.LocalDateTime.now());
            bindings.add(binding);
        }
        
        boolean success = knowledgePointTypeService.saveBatch(bindings);
        
        // 同时更新 knowledge_point 表的 question_types 字段
        for (Long kpId : knowledgePointIds) {
            KnowledgePoint kp = knowledgePointService.getById(kpId);
            if (kp != null) {
                Set<String> types = new HashSet<>();
                if (kp.getQuestionTypes() != null && !kp.getQuestionTypes().isEmpty()) {
                    String existing = kp.getQuestionTypes().replace("[", "").replace("]", "").replace("\"", "");
                    if (!existing.isEmpty()) {
                        Arrays.asList(existing.split(",")).forEach(t -> types.add(t.trim()));
                    }
                }
                types.add(questionType.toUpperCase());
                kp.setQuestionTypes(types.toString().replace(" ", ""));
                knowledgePointService.updateById(kp);
            }
        }
        
        if (success) {
            return Map.of("code", 200, "msg", "绑定成功", "data", bindings.size() + "条记录");
        }
        return Map.of("code", 400, "msg", "绑定失败");
    }
    
    /**
     * 获取知识点的题型绑定列表
     */
    @GetMapping("/type-binding/{knowledgePointId}")
    public Map<String, Object> getTypeBindings(@PathVariable Long knowledgePointId) {
        LambdaQueryWrapper<KnowledgePointType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgePointType::getKnowledgePointId, knowledgePointId);
        List<KnowledgePointType> bindings = knowledgePointTypeService.list(wrapper);
        
        return Map.of("code", 200, "msg", "success", "data", bindings);
    }
    
    private Map<String, Object> buildTreeNode(KnowledgePoint point, List<KnowledgePoint> allPoints) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", point.getId());
        node.put("name", point.getName());
        node.put("subjectId", point.getSubjectId());
        node.put("gradeId", point.getGradeId());
        node.put("parentId", point.getParentId());
        node.put("level", point.getLevel());
        node.put("questionTypes", point.getQuestionTypes());
        node.put("examFreq", point.getExamFreq());
        node.put("scoreWeight", point.getScoreWeight());
        
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
        // 同时删除知识点与题型的绑定关系
        LambdaQueryWrapper<KnowledgePointType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgePointType::getKnowledgePointId, id);
        knowledgePointTypeService.remove(wrapper);
        
        boolean success = knowledgePointService.removeById(id);
        if (success) {
            return Map.of("code", 200, "msg", "删除成功");
        }
        return Map.of("code", 400, "msg", "删除失败");
    }
}
