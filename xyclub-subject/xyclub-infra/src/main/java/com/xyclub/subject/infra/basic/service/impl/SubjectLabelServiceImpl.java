package com.xyclub.subject.infra.basic.service.impl;

import com.xyclub.subject.infra.basic.dao.SubjectLabelDao;
import com.xyclub.subject.infra.basic.entity.SubjectLabel;
import com.xyclub.subject.infra.basic.service.SubjectLabelService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("subjectLabelService")
public class SubjectLabelServiceImpl implements SubjectLabelService {

    @Resource
    private SubjectLabelDao subjectLabelDao;

    @Override
    public SubjectLabel queryById(Long id) {
        return subjectLabelDao.queryById(id);
    }

    @Override
    public int insert(SubjectLabel subjectLabel) {
        return subjectLabelDao.insert(subjectLabel);
    }

    @Override
    public int update(SubjectLabel subjectLabel) {
        return subjectLabelDao.update(subjectLabel);
    }

    @Override
    public boolean deleteById(Long id) {
        return subjectLabelDao.deleteById(id) > 0;
    }

    @Override
    public List<SubjectLabel> batchQueryById(List<Long> labelIdList) {
        return subjectLabelDao.batchQueryById(labelIdList);
    }

    @Override
    public List<SubjectLabel> queryByCondition(SubjectLabel subjectLabel) {
        return subjectLabelDao.queryByCondition(subjectLabel);
    }
}
