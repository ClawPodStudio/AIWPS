package com.aiwps.controller;

import com.aiwps.service.SubjectService;
import com.aiwps.entity.Subject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subject")
public class SubjectController {
    
    @Autowired
    private SubjectService subjectService;
    
    @GetMapping("/list")
    public List<Subject> list() {
        QueryWrapper<Subject> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort_order");
        return subjectService.list(wrapper);
    }
}
