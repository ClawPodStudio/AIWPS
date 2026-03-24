package com.aiwps.service.impl;

import com.aiwps.entity.Tenant;
import com.aiwps.mapper.TenantMapper;
import com.aiwps.service.TenantService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class TenantServiceImpl extends ServiceImpl<TenantMapper, Tenant> implements TenantService {
}
