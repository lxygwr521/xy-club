package com.xyclub.subject.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.xyclub.subject.domain.convert.SubjectCategoryConverter;
import com.xyclub.subject.domain.entity.SubjectCategoryBO;
import com.xyclub.subject.domain.service.SubjectCategoryDomainService;
import com.xyclub.subject.infra.basic.entity.SubjectCategory;
import com.xyclub.subject.infra.basic.service.SubjectCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class SubjectCategoryDomainServiceImpl implements SubjectCategoryDomainService {

    @Resource
    private SubjectCategoryService subjectCategoryService;

    public void add(SubjectCategoryBO subjectCategoryBO){
        if(log.isInfoEnabled()){
            log.info("SubjectCategoryController.add.bo:{}", JSON.toJSONString(subjectCategoryBO));
        }
        SubjectCategory subjectCategory = SubjectCategoryConverter.INSTANCE.convertBoToCategory(subjectCategoryBO);
        subjectCategoryService.insert(subjectCategory);
    }


}
