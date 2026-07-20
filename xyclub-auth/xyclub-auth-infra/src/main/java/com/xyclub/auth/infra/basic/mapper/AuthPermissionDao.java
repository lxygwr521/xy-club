package com.xyclub.auth.infra.basic.mapper;

import com.xyclub.auth.infra.basic.entity.AuthPermission;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * (AuthPermission)表数据库访问层
 *
 * @author lxy
 * @date 2026-07-20
 */
public interface AuthPermissionDao {

    /**
     * 通过ID查询单条数据
     */
    AuthPermission queryById(Long id);

    /**
     * 查询指定行数据
     */
    List<AuthPermission> queryAllByLimit(AuthPermission authPermission);

    /**
     * 统计总行数
     */
    long count(AuthPermission authPermission);

    /**
     * 新增数据
     */
    int insert(AuthPermission authPermission);

    /**
     * 批量新增数据
     */
    int insertBatch(@Param("entities") List<AuthPermission> entities);

    /**
     * 批量新增或按主键更新数据
     */
    int insertOrUpdateBatch(@Param("entities") List<AuthPermission> entities);

    /**
     * 修改数据
     */
    int update(AuthPermission authPermission);

    /**
     * 通过主键删除数据
     */
    int deleteById(Long id);

    List<AuthPermission> queryByRoleList(@Param("list") List<Long> roleIdList);

}
