package com.xyclub.subject.infra.basic.service;

import com.xyclub.subject.common.entity.PageResult;
import com.xyclub.subject.infra.basic.entity.SubjectInfoEs;

public interface SubjectEsService {

    boolean insert(SubjectInfoEs subjectInfoEs);

    PageResult<SubjectInfoEs> querySubjectList(SubjectInfoEs subjectInfoEs);
}
