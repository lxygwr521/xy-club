package com.xyclub.practice.server.dao;

import com.xyclub.practice.server.entity.po.SubjectMultiplePO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 多选题选项数据访问层
 */
public interface SubjectMultipleDao {

    /**
     * 根据题目 id 查询选项
     */
    List<SubjectMultiplePO> selectBySubjectId(@Param("subjectId") Long subjectId);
}
