package com.xyclub.practice.server.dao;

import com.xyclub.practice.server.entity.po.PracticeSetDetailPO;

import java.util.List;

/**
 * 套题内容表(PracticeSetDetail)数据库访问层
 */
public interface PracticeSetDetailDao {

    /**
     * 新增套题内容（一道题目）
     *
     * @param po 套题内容信息
     * @return 受影响行数
     */
    int add(PracticeSetDetailPO po);

    /**
     * 根据套题 id 查询套题包含的题目列表
     *
     * @param setId 套题 id
     * @return 套题内容列表
     */
    List<PracticeSetDetailPO> selectBySetId(Long setId);


}
