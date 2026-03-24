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
    public Map<String, Object> login(String username, String password) {
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
        result.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "name", user.getName(),
                "role", user.getRole(),
                "tenantId", user.getTenantId()
        ));
        return result;
    }
}
