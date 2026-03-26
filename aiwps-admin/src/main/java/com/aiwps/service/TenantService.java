package com.aiwps.service;

import com.aiwps.entity.Tenant;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface TenantService extends IService<Tenant> {
    
    IPage<Tenant> adminList(int page, int size, String name, Integer status);
    
    Tenant getAdminById(Long id);
    
    boolean adminCreate(Tenant tenant);
    
    boolean adminUpdate(Long id, Tenant tenant);
    
    boolean adminDelete(Long id);
    
    long countOrganizations(Long tenantId);
}
