package com.xyclub.auth.infra.basic.service.impl;

import com.xyclub.auth.infra.basic.entity.AuthPermission;
import com.xyclub.auth.infra.basic.mapper.AuthPermissionDao;
import com.xyclub.auth.infra.basic.service.AuthPermissionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * (AuthPermission)表服务实现类
 *
 * @author lxy
 * @date 2026-07-20
 */
@Service("authPermissionService")
public class AuthPermissionServiceImpl implements AuthPermissionService {
    @Resource
    private AuthPermissionDao authPermissionDao;

    @Override
    public AuthPermission queryById(Long id) {
        return this.authPermissionDao.queryById(id);
    }

    @Override
    public int insert(AuthPermission authPermission) {
        return this.authPermissionDao.insert(authPermission);
    }

    @Override
    public int update(AuthPermission authPermission) {
        return this.authPermissionDao.update(authPermission);
    }

    @Override
    public boolean deleteById(Long id) {
        return this.authPermissionDao.deleteById(id) > 0;
    }
}
