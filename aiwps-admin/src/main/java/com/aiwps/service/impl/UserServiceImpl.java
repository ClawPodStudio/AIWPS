package com.aiwps.service.impl;

import com.aiwps.entity.User;
import com.aiwps.mapper.UserMapper;
import com.aiwps.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
