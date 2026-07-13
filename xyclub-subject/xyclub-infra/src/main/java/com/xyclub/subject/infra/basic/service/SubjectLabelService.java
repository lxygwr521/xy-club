package com.xyclub.subject.infra.basic.service;

import com.xyclub.subject.infra.basic.entity.SubjectLabel;

import java.util.List;

public interface SubjectLabelService {

    SubjectLabel queryById(Long id);

    int insert(SubjectLabel subjectLabel);

    int update(SubjectLabel subjectLabel);

    boolean deleteById(Long id);

    List<SubjectLabel> batchQueryById(List<Long> labelIdList);

    List<SubjectLabel> queryByCondition(SubjectLabel subjectLabel);
}
