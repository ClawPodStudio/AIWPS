package com.aiwps.controller;

import com.aiwps.entity.ReviewPlan;
import com.aiwps.service.ReviewPlanService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/review-plan")
public class ReviewPlanController {
    
    @Autowired
    private ReviewPlanService reviewPlanService;
    
    @GetMapping("/today")
    public Map<String, Object> today(@RequestParam(required = false) Long userId) {
        LocalDate today = LocalDate.now();
        
        LambdaQueryWrapper<ReviewPlan> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) wrapper.eq(ReviewPlan::getUserId, userId);
        wrapper.eq(ReviewPlan::getPlanDate, today);
        wrapper.eq(ReviewPlan::getStatus, "PENDING");
        
        var plans = reviewPlanService.list(wrapper);
        
        return Map.of("code", 200, "msg", "success", "data", plans);
    }
    
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        
        LambdaQueryWrapper<ReviewPlan> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) wrapper.eq(ReviewPlan::getUserId, userId);
        if (status != null && !status.isEmpty()) wrapper.eq(ReviewPlan::getStatus, status);
        if (startDate != null) wrapper.ge(ReviewPlan::getPlanDate, startDate);
        if (endDate != null) wrapper.le(ReviewPlan::getPlanDate, endDate);
        
        wrapper.orderByAsc(ReviewPlan::getPlanDate);
        
        Page<ReviewPlan> page = new Page<>(pageNum, pageSize);
        Page<ReviewPlan> result = reviewPlanService.page(page, wrapper);
        
        return Map.of("code", 200, "msg", "success", "data",
                Map.of("records", result.getRecords(), "total", result.getTotal(),
                       "pages", result.getPages(), "current", result.getCurrent()));
    }
    
    @PostMapping
    public Map<String, Object> add(@RequestBody ReviewPlan reviewPlan) {
        boolean success = reviewPlanService.save(reviewPlan);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", reviewPlan);
        }
        return Map.of("code", 400, "msg", "添加失败");
    }
    
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody ReviewPlan reviewPlan) {
        reviewPlan.setId(id);
        boolean success = reviewPlanService.updateById(reviewPlan);
        if (success) {
            return Map.of("code", 200, "msg", "success", "data", reviewPlan);
        }
        return Map.of("code", 400, "msg", "更新失败");
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        boolean success = reviewPlanService.removeById(id);
        if (success) {
            return Map.of("code", 200, "msg", "删除成功");
        }
        return Map.of("code", 400, "msg", "删除失败");
    }
}
