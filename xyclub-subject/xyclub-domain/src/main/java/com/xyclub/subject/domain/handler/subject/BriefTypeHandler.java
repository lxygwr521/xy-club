package com.xyclub.subject.domain.handler.subject;

import com.xyclub.subject.common.enums.IsDeletedFlagEnum;
import com.xyclub.subject.common.enums.SubjectInfoTypeEnum;
import com.xyclub.subject.domain.convert.BriefSubjectConverter;
import com.xyclub.subject.domain.entity.SubjectInfoBO;
import com.xyclub.subject.domain.entity.SubjectOptionBO;
import com.xyclub.subject.infra.basic.entity.SubjectBrief;
import com.xyclub.subject.infra.basic.service.SubjectBriefService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author lxy
 * @date 2026/07/14 14:00
 **/
@Component
public class BriefTypeHandler implements SubjectTypeHandler {

    @Resource
    private SubjectBriefService subjectBriefService;

    @Override
    public SubjectInfoTypeEnum getHandlerType() {
        return SubjectInfoTypeEnum.BRIEF;
    }

    @Override
    public void add(SubjectInfoBO subjectInfoBO) {
        SubjectBrief subjectBrief = BriefSubjectConverter.INSTANCE.convertBoToEntity(subjectInfoBO);
        subjectBrief.setSubjectId(subjectInfoBO.getId().intValue());
        subjectBrief.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        subjectBriefService.insert(subjectBrief);
    }

    @Override
    public SubjectOptionBO query(int subjectId) {
        SubjectBrief subjectBrief = subjectBriefService.queryById(Long.valueOf(subjectId));
        SubjectOptionBO subjectOptionBO = new SubjectOptionBO();
        subjectOptionBO.setSubjectAnswer(subjectBrief.getSubjectAnswer());
        return subjectOptionBO;
    }

}
