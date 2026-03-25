package com.aiwps.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/question-type")
public class QuestionTypeController {
    
    @GetMapping("/list")
    public Map<String, Object> list() {
        List<Map<String, Object>> types = new ArrayList<>();
        types.add(Map.of("id", 1, "code", "CHOICE", "name", "选择题"));
        types.add(Map.of("id", 2, "code", "BLANK", "name", "填空题"));
        types.add(Map.of("id", 3, "code", "FILL", "name", "简答题"));
        types.add(Map.of("id", 4, "code", "DRAWING", "name", "画图题"));
        types.add(Map.of("id", 5, "code", "ESSAY", "name", "论述题"));
        types.add(Map.of("id", 6, "code", "CALCULATION", "name", "计算题"));
        types.add(Map.of("id", 7, "code", "PROOF", "name", "证明题"));
        types.add(Map.of("id", 8, "code", "LISTENING", "name", "听力题"));
        return Map.of("code", 200, "msg", "success", "data", types);
    }
}
