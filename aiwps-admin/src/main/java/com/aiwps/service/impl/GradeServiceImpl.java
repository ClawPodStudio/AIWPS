package com.aiwps.service.impl;

import com.aiwps.entity.Grade;
import com.aiwps.mapper.GradeMapper;
import com.aiwps.service.GradeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class GradeServiceImpl extends ServiceImpl<GradeMapper, Grade> implements GradeService {
}
