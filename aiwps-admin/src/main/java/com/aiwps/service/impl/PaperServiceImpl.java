package com.aiwps.service.impl;

import com.aiwps.entity.Paper;
import com.aiwps.mapper.PaperMapper;
import com.aiwps.service.PaperService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements PaperService {
}
