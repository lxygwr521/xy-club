package com.xyclub.auth.domain.service;

import com.xyclub.auth.domain.entity.AuthRoleBO;

/**
 * 角色领域service
 *
 * @author lxy
 * @date 2026-07-20
 */
public interface AuthRoleDomainService {

    Boolean add(AuthRoleBO authRoleBO);

    Boolean update(AuthRoleBO authRoleBO);

    Boolean delete(AuthRoleBO authRoleBO);

}
