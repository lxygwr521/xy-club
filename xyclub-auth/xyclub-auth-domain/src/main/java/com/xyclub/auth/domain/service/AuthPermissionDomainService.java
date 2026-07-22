package com.xyclub.auth.domain.service;

import com.xyclub.auth.domain.entity.AuthPermissionBO;

import java.util.List;

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

    /**
     * 查询用户权限列表（从 Redis 缓存读取）
     */
    List<String> getPermission(String userName);

}
