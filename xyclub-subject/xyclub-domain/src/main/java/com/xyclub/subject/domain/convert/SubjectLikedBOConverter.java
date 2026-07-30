package com.xyclub.subject.domain.convert;

import com.xyclub.subject.domain.entity.SubjectLikedBO;
import com.xyclub.subject.infra.basic.entity.SubjectLiked;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 题目点赞 BO 转换器。
 */
@Mapper
public interface SubjectLikedBOConverter {

    SubjectLikedBOConverter INSTANCE = Mappers.getMapper(SubjectLikedBOConverter.class);

    /**
     * 将领域 BO 转为数据库实体。
     */
    SubjectLiked convertBOToEntity(SubjectLikedBO subjectLikedBO);

}
