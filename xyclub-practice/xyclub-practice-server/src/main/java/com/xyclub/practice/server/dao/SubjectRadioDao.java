package com.xyclub.practice.server.dao;

import com.xyclub.practice.server.entity.po.SubjectRadioPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 单选/判断题选项数据访问层
 */
public interface SubjectRadioDao {

    /**
     * 根据题目 id 查询选项
     */
    List<SubjectRadioPO> selectBySubjectId(@Param("subjectId") Long subjectId);
}
