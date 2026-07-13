package com.xyclub.subject.application.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SubjectLabelDTO implements Serializable {

    private Long id;

    private Long categoryId;

    private String labelName;

    private Integer sortNum;
}
