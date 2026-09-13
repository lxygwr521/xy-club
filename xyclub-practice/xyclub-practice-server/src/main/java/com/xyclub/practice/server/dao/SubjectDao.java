package com.xyclub.practice.server.dao;

import com.xyclub.practice.server.entity.dto.PracticeSubjectDTO;
import com.xyclub.practice.server.entity.po.SubjectPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 练习题目查询数据库访问层
 */
public interface SubjectDao {

    /**
     * 按分类标签组合随机获取练习题目
     *
     * @param dto 查询条件（分类标签组合、题目类型、数量、排除题目）
     * @return 题目列表
     */
    List<SubjectPO> getPracticeSubject(PracticeSubjectDTO dto);

    /**
     * 根据题目 id 查询题目基础信息
     */
    SubjectPO selectById(@Param("subjectId") Long subjectId);


}
