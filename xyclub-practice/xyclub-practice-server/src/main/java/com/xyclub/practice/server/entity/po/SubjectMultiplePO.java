package com.xyclub.practice.server.entity.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 多选题选项实体
 */
@Data
public class SubjectMultiplePO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long subjectId;
    private Integer optionType;
    private String optionContent;
    private Integer isCorrect;
    private String createdBy;
    private Date createdTime;
    private String updateBy;
    private Integer isDeleted;
    private Date updateTime;
}
