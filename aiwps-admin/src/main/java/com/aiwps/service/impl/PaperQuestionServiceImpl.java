package com.aiwps.service.impl;

import com.aiwps.entity.PaperQuestion;
import com.aiwps.mapper.PaperQuestionMapper;
import com.aiwps.service.PaperQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PaperQuestionServiceImpl extends ServiceImpl<PaperQuestionMapper, PaperQuestion> implements PaperQuestionService {
}
