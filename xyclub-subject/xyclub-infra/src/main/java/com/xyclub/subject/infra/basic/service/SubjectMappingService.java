package com.xyclub.subject.infra.basic.service;

import com.xyclub.subject.infra.basic.entity.SubjectMapping;

import java.util.List;

public interface SubjectMappingService {

    List<SubjectMapping> queryLabelId(SubjectMapping subjectMapping);
    /**
     * 批量插入
     */
    void batchInsert(List<SubjectMapping> mappingList);

}
