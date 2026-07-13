package com.xyclub.subject.infra.basic.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SubjectLabel implements Serializable {

    private Long id;

    private String labelName;

    private Long categoryId;

    private Integer sortNum;

    private String createdBy;

    private Date createdTime;

    private String updateBy;

    private Date updateTime;

    private Integer isDeleted;
}
