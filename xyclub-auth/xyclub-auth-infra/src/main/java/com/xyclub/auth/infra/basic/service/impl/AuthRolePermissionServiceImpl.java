package com.xyclub.auth.infra.basic.service.impl;

import com.xyclub.auth.infra.basic.entity.AuthRolePermission;
import com.xyclub.auth.infra.basic.mapper.AuthRolePermissionDao;
import com.xyclub.auth.infra.basic.service.AuthRolePermissionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * (AuthRolePermission)表服务实现类
 *
 * @author lxy
 * @date 2026-07-20
 */
@Service("authRolePermissionService")
public class AuthRolePermissionServiceImpl implements AuthRolePermissionService {
    @Resource
    private AuthRolePermissionDao authRolePermissionDao;

    @Override
    public AuthRolePermission queryById(Long id) {
        return this.authRolePermissionDao.queryById(id);
    }

    @Override
    public AuthRolePermission insert(AuthRolePermission authRolePermission) {
        this.authRolePermissionDao.insert(authRolePermission);
        return authRolePermission;
    }

    @Override
    public int batchInsert(List<AuthRolePermission> authRolePermissionList) {
        return this.authRolePermissionDao.insertBatch(authRolePermissionList);
    }

    @Override
    public AuthRolePermission update(AuthRolePermission authRolePermission) {
        this.authRolePermissionDao.update(authRolePermission);
        return this.queryById(authRolePermission.getId());
    }

    @Override
    public boolean deleteById(Long id) {
        return this.authRolePermissionDao.deleteById(id) > 0;
    }
}
