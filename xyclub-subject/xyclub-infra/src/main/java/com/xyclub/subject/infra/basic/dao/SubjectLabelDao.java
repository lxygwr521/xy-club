package com.xyclub.subject.infra.basic.dao;

import com.xyclub.subject.infra.basic.entity.SubjectLabel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SubjectLabelDao {

    SubjectLabel queryById(Long id);

    List<SubjectLabel> queryByCondition(SubjectLabel subjectLabel);

    long count(SubjectLabel subjectLabel);

    int insert(SubjectLabel subjectLabel);

    int insertBatch(@Param("entities") List<SubjectLabel> entities);

    int insertOrUpdateBatch(@Param("entities") List<SubjectLabel> entities);

    int update(SubjectLabel subjectLabel);

    int deleteById(Long id);

    List<SubjectLabel> batchQueryById(@Param("list") List<Long> labelIdList);
}
