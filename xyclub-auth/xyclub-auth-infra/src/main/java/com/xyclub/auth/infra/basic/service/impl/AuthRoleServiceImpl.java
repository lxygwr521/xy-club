package com.xyclub.auth.infra.basic.service.impl;

import com.xyclub.auth.infra.basic.entity.AuthRole;
import com.xyclub.auth.infra.basic.mapper.AuthRoleDao;
import com.xyclub.auth.infra.basic.service.AuthRoleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * (AuthRole)表服务实现类
 *
 * @author lxy
 * @date 2026-07-20
 */
@Service("authRoleService")
public class AuthRoleServiceImpl implements AuthRoleService {
    @Resource
    private AuthRoleDao authRoleDao;

    @Override
    public AuthRole queryById(Long id) {
        return this.authRoleDao.queryById(id);
    }

    @Override
    public int insert(AuthRole authRole) {
        return this.authRoleDao.insert(authRole);
    }

    @Override
    public int update(AuthRole authRole) {
        return this.authRoleDao.update(authRole);
    }

    @Override
    public boolean deleteById(Long id) {
        return this.authRoleDao.deleteById(id) > 0;
    }
}
