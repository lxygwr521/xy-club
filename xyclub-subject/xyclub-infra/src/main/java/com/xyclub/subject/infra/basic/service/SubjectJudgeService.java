package com.xyclub.subject.infra.basic.service;

import com.xyclub.subject.infra.basic.entity.SubjectJudge;

import java.util.List;

public interface SubjectJudgeService {

    SubjectJudge queryById(Long id);

    SubjectJudge insert(SubjectJudge subjectJudge);

    SubjectJudge update(SubjectJudge subjectJudge);

    boolean deleteById(Long id);

    List<SubjectJudge> queryByCondition(SubjectJudge subjectJudge);
}
