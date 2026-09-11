package com.xyclub.practice.server.dao;

import com.xyclub.practice.server.entity.po.PracticePO;

/**
 * 练习表(PracticeInfo)数据库访问层
 */
public interface PracticeDao {

    /**
     * 根据练习 id 获取练习详情
     *
     * @param id 练习 id
     * @return 练习信息
     */
    PracticePO selectById(Long id);

    /**
     * 新增练习记录
     *
     * @param practicePO 练习信息
     * @return 受影响行数
     */
    int insert(PracticePO practicePO);

    /**
     * 更新练习详情（交卷时间、用时、完成状态、正确率等）
     *
     * @param practicePO 练习信息
     * @return 受影响行数
     */
    int update(PracticePO practicePO);

}
