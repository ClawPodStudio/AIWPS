package com.aiwps.controller;

import com.aiwps.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
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
        String role = params.get("role"); // 前端传入的角色
        try {
            Map<String, Object> data = userService.login(username, password, role);
            return Map.of("code", 200, "msg", "success", "data", data);
        } catch (Exception e) {
            return Map.of("code", 401, "msg", e.getMessage());
        }
    }
    
    @GetMapping("/info")
    public Map<String, Object> getUserInfo(@RequestParam(required = false) String role) {
        // 根据传入的role返回对应角色信息
        if ("ORG_ADMIN".equals(role)) {
            return Map.of("code", 200, "msg", "success", "data", 
                    Map.of("name", "机构管理员", "role", "ORG_ADMIN"));
        } else if ("TEACHER".equals(role)) {
            return Map.of("code", 200, "msg", "success", "data", 
                    Map.of("name", "教师", "role", "TEACHER"));
        } else if ("STUDENT".equals(role)) {
            return Map.of("code", 200, "msg", "success", "data", 
                    Map.of("name", "学生", "role", "STUDENT"));
        }
        return Map.of("code", 200, "msg", "success", "data", 
                Map.of("name", "管理员", "role", "ADMIN"));
    }
    
    @GetMapping("/list")
    public Map<String, Object> list() {
        return Map.of("code", 200, "msg", "success", "data", userService.list());
    }

    @PostMapping
    public Map<String, Object> register(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        String username = (String) params.get("username");
        String password = (String) params.get("password");
        String role = (String) params.get("role");
        String name = (String) params.get("name");
        
        if (username == null || password == null || role == null) {
            result.put("code", 400);
            result.put("msg", "缺少必要参数");
            return result;
        }
        
        com.aiwps.entity.User user = new com.aiwps.entity.User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        user.setName(name != null ? name : username);
        user.setStatus(1);
        user.setTenantId(1L); // 默认租户
        
        boolean success = userService.save(user);
        if (success) {
            result.put("code", 200);
            result.put("msg", "注册成功");
            result.put("data", user);
        } else {
            result.put("code", 500);
            result.put("msg", "注册失败");
        }
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        com.aiwps.entity.User user = userService.getById(id);
        if (user == null) {
            result.put("code", 404);
            result.put("msg", "用户不存在");
            return result;
        }
        if (params.containsKey("name")) user.setName((String) params.get("name"));
        if (params.containsKey("mobile")) user.setMobile((String) params.get("mobile"));
        boolean success = userService.updateById(user);
        if (success) {
            result.put("code", 200);
            result.put("msg", "更新成功");
            result.put("data", user);
        } else {
            result.put("code", 500);
            result.put("msg", "更新失败");
        }
        return result;
    }
}
