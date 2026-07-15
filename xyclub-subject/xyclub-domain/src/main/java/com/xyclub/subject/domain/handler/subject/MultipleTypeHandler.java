package com.xyclub.subject.domain.handler.subject;

import com.xyclub.subject.common.enums.IsDeletedFlagEnum;
import com.xyclub.subject.common.enums.SubjectInfoTypeEnum;
import com.xyclub.subject.domain.convert.MultipleSubjectConverter;
import com.xyclub.subject.domain.entity.SubjectInfoBO;
import com.xyclub.subject.domain.entity.SubjectOptionBO;
import com.xyclub.subject.infra.basic.entity.SubjectMultiple;
import com.xyclub.subject.infra.basic.service.SubjectMultipleService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
 * @author lxy
 * @date 2026/07/14 14:03
 **/
@Component
public class MultipleTypeHandler implements SubjectTypeHandler{
    @Override
    public SubjectInfoTypeEnum getHandlerType() {
        return SubjectInfoTypeEnum.MULTIPLE;
    }
    @Resource
    SubjectMultipleService subjectMultipleService;

    @Override
    public void add(SubjectInfoBO subjectInfoBO) {
        List<SubjectMultiple> subjectMultipleList = new LinkedList<>();
        subjectInfoBO.getOptionList().forEach(option -> {
            SubjectMultiple  subjectMultiple = MultipleSubjectConverter.INSTANCE.convertBoToEntity(option);
            subjectMultiple.setSubjectId(subjectInfoBO.getId());
            subjectMultiple.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
            subjectMultipleList.add(subjectMultiple);
        });
        subjectMultipleService.batchInsert(subjectMultipleList);
    }

    @Override
    public SubjectOptionBO query(int subjectId) {
        return null;
    }

}
