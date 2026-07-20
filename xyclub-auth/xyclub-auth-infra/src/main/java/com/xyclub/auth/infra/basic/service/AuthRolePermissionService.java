package com.xyclub.auth.infra.basic.service;

import com.xyclub.auth.infra.basic.entity.AuthRolePermission;

import java.util.List;

/**
 * (AuthRolePermission)表服务接口
 *
 * @author lxy
 * @date 2026-07-20
 */
public interface AuthRolePermissionService {

    AuthRolePermission queryById(Long id);

    AuthRolePermission insert(AuthRolePermission authRolePermission);

    int batchInsert(List<AuthRolePermission> authRolePermissionList);

    AuthRolePermission update(AuthRolePermission authRolePermission);

    boolean deleteById(Long id);

}
