package com.xyclub.subject.infra.basic.service;

import com.xyclub.subject.infra.basic.entity.SubjectRadio;

import java.util.List;

public interface SubjectRadioService {

    SubjectRadio queryById(Long id);

    SubjectRadio insert(SubjectRadio subjectRadio);

    void batchInsert(List<SubjectRadio> subjectRadioList);

    SubjectRadio update(SubjectRadio subjectRadio);

    boolean deleteById(Long id);

    List<SubjectRadio> queryByCondition(SubjectRadio subjectRadio);
}
