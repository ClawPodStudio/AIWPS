package com.aiwps.service.impl;

import com.aiwps.entity.KnowledgePoint;
import com.aiwps.mapper.KnowledgePointMapper;
import com.aiwps.service.KnowledgePointService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class KnowledgePointServiceImpl extends ServiceImpl<KnowledgePointMapper, KnowledgePoint> implements KnowledgePointService {
}
