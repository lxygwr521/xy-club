package com.xyclub.auth.infra.basic.mapper;

import com.xyclub.auth.infra.basic.entity.AuthRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (AuthRole)表数据库访问层
 *
 * @author lxy
 * @date 2026-07-20
 */
public interface AuthRoleDao {

    /**
     * 通过ID查询单条数据
     */
    AuthRole queryById(Long id);

    /**
     * 查询指定行数据
     */
    AuthRole queryAllByLimit(AuthRole authRole);

    /**
     * 统计总行数
     */
    long count(AuthRole authRole);

    /**
     * 新增数据
     */
    int insert(AuthRole authRole);

    /**
     * 批量新增数据
     */
    int insertBatch(@Param("entities") List<AuthRole> entities);

    /**
     * 批量新增或按主键更新数据
     */
    int insertOrUpdateBatch(@Param("entities") List<AuthRole> entities);

    /**
     * 修改数据
     */
    int update(AuthRole authRole);

    /**
     * 通过主键删除数据
     */
    int deleteById(Long id);

    List<AuthRole> queryByRoleList(@Param("list") List<Long> roleIdList);

}
