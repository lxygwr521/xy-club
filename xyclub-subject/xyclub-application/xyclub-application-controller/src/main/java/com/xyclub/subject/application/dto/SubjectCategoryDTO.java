package com.xyclub.subject.application.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SubjectCategoryDTO implements Serializable {

    private Long id;

    private String categoryName;

    private Integer categoryType;

    private String imageUrl;

    private Long parentId;

    /**
     * 分类下题目数量
     */
    private Integer count;

    /**
     * 分类下的标签列表
     */
    private List<SubjectLabelDTO> labelDTOList;

}
