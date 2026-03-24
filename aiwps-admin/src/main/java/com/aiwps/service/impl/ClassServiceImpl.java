package com.aiwps.service.impl;

import com.aiwps.entity.ClassInfo;
import com.aiwps.service.ClassService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ClassServiceImpl extends ServiceImpl<ClassMapper, ClassInfo> implements ClassService {
}
