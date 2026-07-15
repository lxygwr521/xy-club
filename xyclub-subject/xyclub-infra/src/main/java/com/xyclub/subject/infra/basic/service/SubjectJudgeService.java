package com.xyclub.subject.infra.basic.service;

import com.xyclub.subject.infra.basic.entity.SubjectJudge;

public interface SubjectJudgeService {

    SubjectJudge queryById(Long id);

    SubjectJudge insert(SubjectJudge subjectJudge);

    SubjectJudge update(SubjectJudge subjectJudge);

    boolean deleteById(Long id);
}
