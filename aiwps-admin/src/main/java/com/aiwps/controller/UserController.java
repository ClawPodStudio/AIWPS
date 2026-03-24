package com.aiwps.controller;

import com.aiwps.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        try {
            Map<String, Object> data = userService.login(username, password);
            return Map.of("code", 200, "msg", "success", "data", data);
        } catch (Exception e) {
            return Map.of("code", 401, "msg", e.getMessage());
        }
    }
    
    @GetMapping("/info")
    public Map<String, Object> getUserInfo() {
        return Map.of("code", 200, "msg", "success", "data", 
                Map.of("name", "管理员", "role", "ADMIN"));
    }
    
    @GetMapping("/list")
    public Map<String, Object> list() {
        return Map.of("code", 200, "msg", "success", "data", userService.list());
    }
}
