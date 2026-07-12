package com.xyclub.subject.application.convert;

import com.xyclub.subject.application.dto.SubjectCategoryDTO;
import com.xyclub.subject.domain.entity.SubjectCategoryBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SubjectCategoryDTOConverter {

    SubjectCategoryDTOConverter INSTANCE = Mappers.getMapper(SubjectCategoryDTOConverter.class);

    SubjectCategoryBO convertDtoToBO(SubjectCategoryDTO subjectCategoryDTO);

    SubjectCategoryDTO convertBOToDTO(SubjectCategoryBO subjectCategoryBO);

    List<SubjectCategoryDTO> convertBOToDTOList(List<SubjectCategoryBO> subjectCategoryBOList);
}
