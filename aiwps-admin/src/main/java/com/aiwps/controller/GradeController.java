package com.aiwps.controller;

import com.aiwps.service.GradeService;
import com.aiwps.entity.Grade;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grade")
public class GradeController {
    
    @Autowired
    private GradeService gradeService;
    
    @GetMapping("/list")
    public List<Grade> list() {
        QueryWrapper<Grade> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort_order");
        return gradeService.list(wrapper);
    }
}
