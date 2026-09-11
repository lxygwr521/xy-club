package com.xyclub.practice.server.dao;

import com.xyclub.practice.server.entity.po.PracticeSetPO;

/**
 * 练习套题表(PracticeSet)数据库访问层
 */
public interface PracticeSetDao {

    /**
     * 新增套题
     *
     * @param po 套题信息
     * @return 受影响行数
     */
    int add(PracticeSetPO po);

    /**
     * 根据主键查询套题
     *
     * @param setId 套题 id
     * @return 套题信息
     */
    PracticeSetPO selectById(Long setId);

    /**
     * 套题热度 +1（每次交卷调用）
     *
     * @param setId 套题 id
     * @return 受影响行数
     */
    int updateHeat(Long setId);

}
