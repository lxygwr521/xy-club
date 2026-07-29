package com.xyclub.subject.application.convert;

import com.xyclub.subject.application.dto.SubjectAnswerDTO;
import com.xyclub.subject.application.dto.SubjectInfoDTO;
import com.xyclub.subject.domain.entity.SubjectAnswerBO;
import com.xyclub.subject.domain.entity.SubjectInfoBO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-29T12:57:11+0800",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 11.0.15.1 (Oracle Corporation)"
)
public class SubjectInfoDTOConverterImpl implements SubjectInfoDTOConverter {

    @Override
    public SubjectInfoBO convertDTOToBO(SubjectInfoDTO subjectInfoDTO) {
        if ( subjectInfoDTO == null ) {
            return null;
        }

        SubjectInfoBO subjectInfoBO = new SubjectInfoBO();

        subjectInfoBO.setPageNo( subjectInfoDTO.getPageNo() );
        subjectInfoBO.setPageSize( subjectInfoDTO.getPageSize() );
        subjectInfoBO.setId( subjectInfoDTO.getId() );
        subjectInfoBO.setSubjectName( subjectInfoDTO.getSubjectName() );
        subjectInfoBO.setSubjectDifficult( subjectInfoDTO.getSubjectDifficult() );
        subjectInfoBO.setSettleName( subjectInfoDTO.getSettleName() );
        subjectInfoBO.setSubjectType( subjectInfoDTO.getSubjectType() );
        subjectInfoBO.setSubjectScore( subjectInfoDTO.getSubjectScore() );
        subjectInfoBO.setSubjectParse( subjectInfoDTO.getSubjectParse() );
        subjectInfoBO.setSubjectAnswer( subjectInfoDTO.getSubjectAnswer() );
        List<Integer> list = subjectInfoDTO.getCategoryIds();
        if ( list != null ) {
            subjectInfoBO.setCategoryIds( new ArrayList<Integer>( list ) );
        }
        List<Integer> list1 = subjectInfoDTO.getLabelIds();
        if ( list1 != null ) {
            subjectInfoBO.setLabelIds( new ArrayList<Integer>( list1 ) );
        }
        List<String> list2 = subjectInfoDTO.getLabelName();
        if ( list2 != null ) {
            subjectInfoBO.setLabelName( new ArrayList<String>( list2 ) );
        }
        subjectInfoBO.setOptionList( subjectAnswerDTOListToSubjectAnswerBOList( subjectInfoDTO.getOptionList() ) );
        subjectInfoBO.setCategoryId( subjectInfoDTO.getCategoryId() );
        subjectInfoBO.setLabelId( subjectInfoDTO.getLabelId() );
        subjectInfoBO.setKeyWord( subjectInfoDTO.getKeyWord() );

        return subjectInfoBO;
    }

    @Override
    public SubjectInfoDTO convertBOToDTO(SubjectInfoBO subjectInfoBO) {
        if ( subjectInfoBO == null ) {
            return null;
        }

        SubjectInfoDTO subjectInfoDTO = new SubjectInfoDTO();

        subjectInfoDTO.setPageNo( subjectInfoBO.getPageNo() );
        subjectInfoDTO.setPageSize( subjectInfoBO.getPageSize() );
        subjectInfoDTO.setId( subjectInfoBO.getId() );
        subjectInfoDTO.setSubjectName( subjectInfoBO.getSubjectName() );
        subjectInfoDTO.setSubjectDifficult( subjectInfoBO.getSubjectDifficult() );
        subjectInfoDTO.setSettleName( subjectInfoBO.getSettleName() );
        subjectInfoDTO.setSubjectType( subjectInfoBO.getSubjectType() );
        subjectInfoDTO.setSubjectScore( subjectInfoBO.getSubjectScore() );
        subjectInfoDTO.setSubjectParse( subjectInfoBO.getSubjectParse() );
        subjectInfoDTO.setSubjectAnswer( subjectInfoBO.getSubjectAnswer() );
        List<Integer> list = subjectInfoBO.getCategoryIds();
        if ( list != null ) {
            subjectInfoDTO.setCategoryIds( new ArrayList<Integer>( list ) );
        }
        List<Integer> list1 = subjectInfoBO.getLabelIds();
        if ( list1 != null ) {
            subjectInfoDTO.setLabelIds( new ArrayList<Integer>( list1 ) );
        }
        List<String> list2 = subjectInfoBO.getLabelName();
        if ( list2 != null ) {
            subjectInfoDTO.setLabelName( new ArrayList<String>( list2 ) );
        }
        subjectInfoDTO.setOptionList( subjectAnswerBOListToSubjectAnswerDTOList( subjectInfoBO.getOptionList() ) );
        subjectInfoDTO.setCategoryId( subjectInfoBO.getCategoryId() );
        subjectInfoDTO.setLabelId( subjectInfoBO.getLabelId() );
        subjectInfoDTO.setKeyWord( subjectInfoBO.getKeyWord() );

        return subjectInfoDTO;
    }

    protected SubjectAnswerBO subjectAnswerDTOToSubjectAnswerBO(SubjectAnswerDTO subjectAnswerDTO) {
        if ( subjectAnswerDTO == null ) {
            return null;
        }

        SubjectAnswerBO subjectAnswerBO = new SubjectAnswerBO();

        subjectAnswerBO.setOptionType( subjectAnswerDTO.getOptionType() );
        subjectAnswerBO.setOptionContent( subjectAnswerDTO.getOptionContent() );
        subjectAnswerBO.setIsCorrect( subjectAnswerDTO.getIsCorrect() );

        return subjectAnswerBO;
    }

    protected List<SubjectAnswerBO> subjectAnswerDTOListToSubjectAnswerBOList(List<SubjectAnswerDTO> list) {
        if ( list == null ) {
            return null;
        }

        List<SubjectAnswerBO> list1 = new ArrayList<SubjectAnswerBO>( list.size() );
        for ( SubjectAnswerDTO subjectAnswerDTO : list ) {
            list1.add( subjectAnswerDTOToSubjectAnswerBO( subjectAnswerDTO ) );
        }

        return list1;
    }

    protected SubjectAnswerDTO subjectAnswerBOToSubjectAnswerDTO(SubjectAnswerBO subjectAnswerBO) {
        if ( subjectAnswerBO == null ) {
            return null;
        }

        SubjectAnswerDTO subjectAnswerDTO = new SubjectAnswerDTO();

        subjectAnswerDTO.setOptionType( subjectAnswerBO.getOptionType() );
        subjectAnswerDTO.setOptionContent( subjectAnswerBO.getOptionContent() );
        subjectAnswerDTO.setIsCorrect( subjectAnswerBO.getIsCorrect() );

        return subjectAnswerDTO;
    }

    protected List<SubjectAnswerDTO> subjectAnswerBOListToSubjectAnswerDTOList(List<SubjectAnswerBO> list) {
        if ( list == null ) {
            return null;
        }

        List<SubjectAnswerDTO> list1 = new ArrayList<SubjectAnswerDTO>( list.size() );
        for ( SubjectAnswerBO subjectAnswerBO : list ) {
            list1.add( subjectAnswerBOToSubjectAnswerDTO( subjectAnswerBO ) );
        }

        return list1;
    }
}
