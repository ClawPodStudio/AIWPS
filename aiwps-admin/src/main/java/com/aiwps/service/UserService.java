package com.aiwps.service;

import com.aiwps.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.Map;

public interface UserService extends IService<User> {
    Map<String, Object> login(String username, String password);
}
