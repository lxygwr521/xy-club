package com.xyclub.subject.infra.basic.service.impl;

import com.xyclub.subject.infra.basic.dao.SubjectBriefDao;
import com.xyclub.subject.infra.basic.entity.SubjectBrief;
import com.xyclub.subject.infra.basic.service.SubjectBriefService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("subjectBriefService")
public class SubjectBriefServiceImpl implements SubjectBriefService {

    @Resource
    private SubjectBriefDao subjectBriefDao;

    @Override
    public SubjectBrief queryById(Long id) {
        return subjectBriefDao.queryById(id);
    }

    @Override
    public SubjectBrief insert(SubjectBrief subjectBrief) {
        subjectBriefDao.insert(subjectBrief);
        return subjectBrief;
    }

    @Override
    public SubjectBrief update(SubjectBrief subjectBrief) {
        subjectBriefDao.update(subjectBrief);
        return queryById(subjectBrief.getId());
    }

    @Override
    public boolean deleteById(Long id) {
        return subjectBriefDao.deleteById(id) > 0;
    }
}
