package com.aiwps.controller;

import com.aiwps.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/question")
public class QuestionController {
    
    @Autowired
    private QuestionService questionService;
}
