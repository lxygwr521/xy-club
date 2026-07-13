package com.xyclub.subject.infra.basic.service.impl;

import com.xyclub.subject.infra.basic.dao.SubjectMappingDao;
import com.xyclub.subject.infra.basic.entity.SubjectMapping;
import com.xyclub.subject.infra.basic.service.SubjectMappingService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("subjectMappingService")
public class SubjectMappingServiceImpl implements SubjectMappingService {

    @Resource
    private SubjectMappingDao subjectMappingDao;

    @Override
    public List<SubjectMapping> queryLabelId(SubjectMapping subjectMapping) {
        return subjectMappingDao.queryDistinctLabelId(subjectMapping);
    }
}
