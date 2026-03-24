package com.aiwps.service.impl;

import com.aiwps.entity.Organization;
import com.aiwps.mapper.OrganizationMapper;
import com.aiwps.service.OrganizationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> implements OrganizationService {
}
