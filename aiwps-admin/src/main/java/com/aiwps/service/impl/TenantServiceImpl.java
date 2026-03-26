package com.aiwps.service.impl;

import com.aiwps.entity.Organization;
import com.aiwps.entity.Tenant;
import com.aiwps.entity.User;
import com.aiwps.mapper.OrganizationMapper;
import com.aiwps.mapper.TenantMapper;
import com.aiwps.mapper.UserMapper;
import com.aiwps.service.TenantService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TenantServiceImpl extends ServiceImpl<TenantMapper, Tenant> implements TenantService {
    
    @Autowired
    private OrganizationMapper organizationMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public IPage<Tenant> adminList(int page, int size, String name, Integer status) {
        Page<Tenant> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Tenant::getName, name);
        }
        if (status != null) {
            wrapper.eq(Tenant::getStatus, status);
        }
        wrapper.orderByDesc(Tenant::getCreatedAt);
        return this.page(pageParam, wrapper);
    }
    
    @Override
    public Tenant getAdminById(Long id) {
        return this.getById(id);
    }
    
    @Override
    public boolean adminCreate(Tenant tenant) {
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setUpdatedAt(LocalDateTime.now());
        return this.save(tenant);
    }
    
    @Override
    public boolean adminUpdate(Long id, Tenant tenant) {
        tenant.setId(id);
        tenant.setUpdatedAt(LocalDateTime.now());
        return this.updateById(tenant);
    }
    
    @Override
    @Transactional
    public boolean adminDelete(Long id) {
        // Delete related users first
        userMapper.delete(new LambdaQueryWrapper<User>().eq(User::getTenantId, id));
        // Delete related organizations
        organizationMapper.delete(new LambdaQueryWrapper<Organization>().eq(Organization::getTenantId, id));
        // Delete tenant
        return this.removeById(id);
    }
    
    @Override
    public long countOrganizations(Long tenantId) {
        return organizationMapper.selectCount(new LambdaQueryWrapper<Organization>().eq(Organization::getTenantId, tenantId));
    }
}
