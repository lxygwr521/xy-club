package com.xyclub.subject.infra.basic.dao;

import com.xyclub.subject.infra.basic.entity.SubjectMultiple;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SubjectMultipleDao {

    SubjectMultiple queryById(Long id);

    List<SubjectMultiple> queryAllByLimit(SubjectMultiple subjectMultiple);

    long count(SubjectMultiple subjectMultiple);

    int insert(SubjectMultiple subjectMultiple);

    int insertBatch(@Param("entities") List<SubjectMultiple> entities);

    int insertOrUpdateBatch(@Param("entities") List<SubjectMultiple> entities);

    int update(SubjectMultiple subjectMultiple);

    int deleteById(Long id);
}
