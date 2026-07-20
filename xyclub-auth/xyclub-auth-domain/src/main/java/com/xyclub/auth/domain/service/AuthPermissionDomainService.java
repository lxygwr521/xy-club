package com.xyclub.auth.domain.service;

import com.xyclub.auth.domain.entity.AuthPermissionBO;

/**
 * 权限领域service
 *
 * @author lxy
 * @date 2026-07-20
 */
public interface AuthPermissionDomainService {

    Boolean add(AuthPermissionBO authPermissionBO);

    Boolean update(AuthPermissionBO authPermissionBO);

    Boolean delete(AuthPermissionBO authPermissionBO);

}
