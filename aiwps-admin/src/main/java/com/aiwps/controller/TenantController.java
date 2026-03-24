package com.aiwps.controller;

import com.aiwps.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenant")
public class TenantController {
    
    @Autowired
    private TenantService tenantService;
}
