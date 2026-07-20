package com.xyclub.auth.infra.basic.service;

import com.xyclub.auth.infra.basic.entity.AuthUserRole;

/**
 * (AuthUserRole)表服务接口
 *
 * @author lxy
 * @date 2026-07-20
 */
public interface AuthUserRoleService {

    /**
     * 通过ID查询单条数据
     */
    AuthUserRole queryById(Long id);

    /**
     * 新增数据
     */
    AuthUserRole insert(AuthUserRole authUserRole);

    /**
     * 修改数据
     */
    AuthUserRole update(AuthUserRole authUserRole);

    /**
     * 通过主键删除数据
     */
    boolean deleteById(Long id);

}
