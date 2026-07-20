package com.xyclub.auth.infra.basic.service;

import com.xyclub.auth.infra.basic.entity.AuthRole;

/**
 * (AuthRole)表服务接口
 *
 * @author lxy
 * @date 2026-07-20
 */
public interface AuthRoleService {

    /**
     * 通过ID查询单条数据
     */
    AuthRole queryById(Long id);

    /**
     * 新增数据
     */
    int insert(AuthRole authRole);

    /**
     * 修改数据
     */
    int update(AuthRole authRole);

    /**
     * 通过主键删除数据
     */
    boolean deleteById(Long id);

}
