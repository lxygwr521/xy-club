package com.xyclub.practice.server.dao;

import com.xyclub.practice.server.entity.po.SubjectJudgePO;
import org.apache.ibatis.annotations.Param;

/**
 * 判断题标准答案数据访问层。
 */
public interface SubjectJudgeDao {

    /**
     * 根据题目 id 查询未删除的判断题标准答案。
     *
     * @param subjectId 题目 id
     * @return 判断题答案，不存在时返回 null
     */
    SubjectJudgePO selectBySubjectId(@Param("subjectId") Long subjectId);
}
