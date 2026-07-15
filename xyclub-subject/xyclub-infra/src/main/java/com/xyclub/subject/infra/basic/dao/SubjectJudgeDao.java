package com.xyclub.subject.infra.basic.dao;

import com.xyclub.subject.infra.basic.entity.SubjectJudge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SubjectJudgeDao {

    SubjectJudge queryById(Long id);

    List<SubjectJudge> queryAllByLimit(SubjectJudge subjectJudge);

    long count(SubjectJudge subjectJudge);

    int insert(SubjectJudge subjectJudge);

    int insertBatch(@Param("entities") List<SubjectJudge> entities);

    int insertOrUpdateBatch(@Param("entities") List<SubjectJudge> entities);

    int update(SubjectJudge subjectJudge);

    int deleteById(Long id);
}
