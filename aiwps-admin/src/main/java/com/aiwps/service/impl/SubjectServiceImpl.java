package com.aiwps.service.impl;

import com.aiwps.entity.Subject;
import com.aiwps.mapper.SubjectMapper;
import com.aiwps.service.SubjectService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SubjectServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements SubjectService {
}
