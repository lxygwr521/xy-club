package com.xyclub.subject.domain.service;

import com.xyclub.subject.common.entity.PageResult;
import com.xyclub.subject.domain.entity.SubjectInfoBO;

/**
 * @author lxy
 * @date 2026/07/14 14:24
 **/

public interface SubjectInfoDomainService {
    /**
     * 新增题目
     */
    void add(SubjectInfoBO subjectInfoBO);

    /**
     * 分页查询
     */
    PageResult<SubjectInfoBO> getSubjectPage(SubjectInfoBO subjectInfoBO);

    /**
     * 查询题目信息
     */
    SubjectInfoBO querySubjectInfo(SubjectInfoBO subjectInfoBO);


}
