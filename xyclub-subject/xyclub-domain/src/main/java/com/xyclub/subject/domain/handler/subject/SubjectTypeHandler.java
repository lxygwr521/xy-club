package com.xyclub.subject.domain.handler.subject;

import com.xyclub.subject.common.enums.SubjectInfoTypeEnum;
import com.xyclub.subject.domain.entity.SubjectInfoBO;
import com.xyclub.subject.domain.entity.SubjectOptionBO;

public interface SubjectTypeHandler {
    /**
     * 枚举身份的识别
     */
    SubjectInfoTypeEnum getHandlerType();

    /**
     * 实际的题目的插入
     */
    void add(SubjectInfoBO subjectInfoBO);

    /**
     * 瀹為檯鐨勯鐩殑鏌ヨ
     */
    SubjectOptionBO query(int subjectId);

}
