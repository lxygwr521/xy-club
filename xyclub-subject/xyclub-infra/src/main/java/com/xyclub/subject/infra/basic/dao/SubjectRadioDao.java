package com.xyclub.subject.infra.basic.dao;

import com.xyclub.subject.infra.basic.entity.SubjectRadio;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SubjectRadioDao {

    SubjectRadio queryById(Long id);

    List<SubjectRadio> queryAllByLimit(SubjectRadio subjectRadio);

    long count(SubjectRadio subjectRadio);

    int insert(SubjectRadio subjectRadio);

    int insertBatch(@Param("entities") List<SubjectRadio> entities);

    int insertOrUpdateBatch(@Param("entities") List<SubjectRadio> entities);

    int update(SubjectRadio subjectRadio);

    int deleteById(Long id);
}
