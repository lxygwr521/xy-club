package com.xyclub.auth.infra.basic.mapper;

import com.xyclub.auth.infra.basic.entity.AuthUserRole;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * (AuthUserRole)表数据库访问层
 *
 * @author lxy
 * @date 2026-07-20
 */
public interface AuthUserRoleDao {

    /**
     * 通过ID查询单条数据
     */
    AuthUserRole queryById(Long id);

    /**
     * 查询指定行数据
     */
    List<AuthUserRole> queryAllByLimit(AuthUserRole authUserRole);

    /**
     * 统计总行数
     */
    long count(AuthUserRole authUserRole);

    /**
     * 新增数据
     */
    int insert(AuthUserRole authUserRole);

    /**
     * 批量新增数据
     */
    int insertBatch(@Param("entities") List<AuthUserRole> entities);

    /**
     * 批量新增或按主键更新数据
     */
    int insertOrUpdateBatch(@Param("entities") List<AuthUserRole> entities);

    /**
     * 修改数据
     */
    int update(AuthUserRole authUserRole);

    /**
     * 通过主键删除数据
     */
    int deleteById(Long id);

}
