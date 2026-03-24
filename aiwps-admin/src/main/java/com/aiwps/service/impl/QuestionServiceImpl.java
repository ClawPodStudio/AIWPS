package com.aiwps.service.impl;

import com.aiwps.entity.Question;
import com.aiwps.mapper.QuestionMapper;
import com.aiwps.service.QuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
}
