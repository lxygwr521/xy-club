package com.xyclub.subject.infra.basic.dao;

import com.xyclub.subject.infra.basic.entity.SubjectMapping;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SubjectMappingDao {

    List<SubjectMapping> queryDistinctLabelId(SubjectMapping subjectMapping);
}
