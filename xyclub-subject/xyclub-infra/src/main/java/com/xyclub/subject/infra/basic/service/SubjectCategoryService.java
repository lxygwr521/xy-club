package com.xyclub.subject.infra.basic.service;

import com.xyclub.subject.infra.basic.entity.SubjectCategory;

import java.util.List;

public interface SubjectCategoryService {

    SubjectCategory queryById(Long id);


    List<SubjectCategory> queryCategory(SubjectCategory subjectCategory);

    SubjectCategory insert(SubjectCategory subjectCategory);

    int update(SubjectCategory subjectCategory);

    boolean deleteById(Long id);
}
