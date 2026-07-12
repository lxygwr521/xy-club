package com.xyclub.subject.application.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SubjectCategoryDTO implements Serializable {

    private Long id;

    private String categoryName;

    private Integer categoryType;

    private String imageUrl;

    private Long parentId;

    private Integer count;
}
