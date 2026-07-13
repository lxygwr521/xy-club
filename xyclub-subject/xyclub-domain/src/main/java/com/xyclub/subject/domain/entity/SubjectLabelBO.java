package com.xyclub.subject.domain.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class SubjectLabelBO implements Serializable {

    private Long id;

    private String labelName;

    private Integer sortNum;

    private Long categoryId;
}
