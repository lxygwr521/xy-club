package com.xyclub.subject.infra.basic.service;

import com.xyclub.subject.infra.basic.entity.SubjectBrief;

public interface SubjectBriefService {

    SubjectBrief queryById(Long id);

    SubjectBrief insert(SubjectBrief subjectBrief);

    SubjectBrief update(SubjectBrief subjectBrief);

    boolean deleteById(Long id);

    SubjectBrief queryByCondition(SubjectBrief subjectBrief);
}
