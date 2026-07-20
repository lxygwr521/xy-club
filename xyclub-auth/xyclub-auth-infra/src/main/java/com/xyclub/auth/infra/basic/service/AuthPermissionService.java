package com.xyclub.auth.infra.basic.service;

import com.xyclub.auth.infra.basic.entity.AuthPermission;

/**
 * (AuthPermission)表服务接口
 *
 * @author lxy
 * @date 2026-07-20
 */
public interface AuthPermissionService {

    /**
     * 通过ID查询单条数据
     */
    AuthPermission queryById(Long id);

    /**
     * 新增数据
     */
    int insert(AuthPermission authPermission);

    /**
     * 修改数据
     */
    int update(AuthPermission authPermission);

    /**
     * 通过主键删除数据
     */
    boolean deleteById(Long id);

}
