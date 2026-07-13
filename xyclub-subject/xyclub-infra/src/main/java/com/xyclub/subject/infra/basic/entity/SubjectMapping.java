package com.xyclub.subject.infra.basic.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SubjectMapping implements Serializable {

    private Long id;

    private Long subjectId;

    private Long categoryId;

    private Long labelId;

    private String createdBy;

    private Date createdTime;

    private String updateBy;

    private Date updateTime;

    private Integer isDeleted;
}
