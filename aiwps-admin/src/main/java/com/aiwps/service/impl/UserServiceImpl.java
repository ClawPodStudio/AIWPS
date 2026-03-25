package com.aiwps.service.impl;

import com.aiwps.entity.User;
import com.aiwps.mapper.UserMapper;
import com.aiwps.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Override
    public Map<String, Object> login(String username, String password, String role) {
        User user = this.lambdaQuery()
                .eq(User::getUsername, username)
                .eq(User::getPassword, password)
                .eq(User::getStatus, 1)
                .one();
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        String token = UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        // 优先使用前端传入的role（支持同一账号不同角色登录）
        String userRole = (role != null && !role.isEmpty()) ? role : user.getRole();
        result.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "name", user.getName(),
                "role", userRole,
                "tenantId", user.getTenantId()
        ));
        return result;
    }
}
