package com.xyclub.subject.application.convert;

import com.xyclub.subject.application.dto.SubjectLikedDTO;
import com.xyclub.subject.domain.entity.SubjectLikedBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 题目点赞 DTO 转换器。
 */
@Mapper
public interface SubjectLikedDTOConverter {

    SubjectLikedDTOConverter INSTANCE = Mappers.getMapper(SubjectLikedDTOConverter.class);

    /**
     * 将接口层 DTO 转为领域 BO。
     */
    SubjectLikedBO convertDTOToBO(SubjectLikedDTO subjectLikedDTO);

}
