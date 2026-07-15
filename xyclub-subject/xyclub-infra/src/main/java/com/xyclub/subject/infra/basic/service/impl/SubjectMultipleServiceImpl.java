package com.xyclub.subject.infra.basic.service.impl;

import com.xyclub.subject.infra.basic.dao.SubjectMultipleDao;
import com.xyclub.subject.infra.basic.entity.SubjectMultiple;
import com.xyclub.subject.infra.basic.service.SubjectMultipleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("subjectMultipleService")
public class SubjectMultipleServiceImpl implements SubjectMultipleService {

    @Resource
    private SubjectMultipleDao subjectMultipleDao;

    @Override
    public SubjectMultiple queryById(Long id) {
        return subjectMultipleDao.queryById(id);
    }

    @Override
    public SubjectMultiple insert(SubjectMultiple subjectMultiple) {
        subjectMultipleDao.insert(subjectMultiple);
        return subjectMultiple;
    }

    @Override
    public void batchInsert(List<SubjectMultiple> subjectMultipleList) {
        subjectMultipleDao.insertBatch(subjectMultipleList);
    }

    @Override
    public SubjectMultiple update(SubjectMultiple subjectMultiple) {
        subjectMultipleDao.update(subjectMultiple);
        return queryById(subjectMultiple.getId());
    }

    @Override
    public boolean deleteById(Long id) {
        return subjectMultipleDao.deleteById(id) > 0;
    }
}
