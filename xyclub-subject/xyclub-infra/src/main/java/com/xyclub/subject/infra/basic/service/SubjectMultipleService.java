package com.xyclub.subject.infra.basic.service;

import com.xyclub.subject.infra.basic.entity.SubjectMultiple;

import java.util.List;

public interface SubjectMultipleService {

    SubjectMultiple queryById(Long id);

    SubjectMultiple insert(SubjectMultiple subjectMultiple);

    SubjectMultiple update(SubjectMultiple subjectMultiple);

    boolean deleteById(Long id);

    void batchInsert(List<SubjectMultiple> subjectMultipleList);

    List<SubjectMultiple> queryByCondition(SubjectMultiple subjectMultiple);
}
