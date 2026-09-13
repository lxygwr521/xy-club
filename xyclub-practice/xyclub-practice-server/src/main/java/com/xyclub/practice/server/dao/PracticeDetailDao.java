package com.xyclub.practice.server.dao;

import com.xyclub.practice.server.entity.po.PracticeDetailPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 练习详情表(PracticeDetail)数据库访问层
 */
public interface PracticeDetailDao {

    /**
     * 获取某次练习的正确答案数量
     *
     * @param practiceId 练习 id
     * @return 正确题数
     */
    int selectCorrectCount(Long practiceId);

    /**
     * 获取得分情况（某次练习的全部作答记录）
     *
     * @param practiceId 练习 id
     * @return 作答明细列表
     */
    List<PracticeDetailPO> selectByPracticeId(Long practiceId);

    /**
     * 插入单条练题作答记录
     *
     * @param practiceDetailPO 作答明细
     * @return 受影响行数
     */
    int insertSingle(PracticeDetailPO practiceDetailPO);

    /**
     * 根据练习 id、题目 id 查询作答详情（用于继续练习时恢复进度）
     *
     * @param practiceId 练习 id
     * @param subjectId  题目 id
     * @param loginId    当前登录用户
     * @return 作答明细
     */
    PracticeDetailPO selectDetail(@Param("practiceId") Long practiceId,
                                  @Param("subjectId") Long subjectId,
                                  @Param("loginId") String loginId);

    /**
     * 更新已经存在的作答记录，主要用于用户返回上一题后重新选择答案。
     *
     * @param practiceDetailPO 包含记录 id、答案内容和判题结果
     * @return 受影响行数
     */
    int update(PracticeDetailPO practiceDetailPO);

    /**
     * 查询某次练习中指定题目的用户答案。
     *
     * @param practiceId 练习 id
     * @param subjectId  题目 id
     * @return 对应的作答记录
     */
    PracticeDetailPO selectAnswer(@Param("practiceId") Long practiceId,
                                  @Param("subjectId") Long subjectId);

}
