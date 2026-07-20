package com.xyclub.auth.infra.basic.mapper;

import com.xyclub.auth.infra.basic.entity.AuthRolePermission;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * (AuthRolePermission)表数据库访问层
 *
 * @author lxy
 * @date 2026-07-20
 */
public interface AuthRolePermissionDao {

    AuthRolePermission queryById(Long id);

    List<AuthRolePermission> queryAllByLimit(AuthRolePermission authRolePermission);

    long count(AuthRolePermission authRolePermission);

    int insert(AuthRolePermission authRolePermission);

    int insertBatch(@Param("entities") List<AuthRolePermission> entities);

    int insertOrUpdateBatch(@Param("entities") List<AuthRolePermission> entities);

    int update(AuthRolePermission authRolePermission);

    int deleteById(Long id);

}
