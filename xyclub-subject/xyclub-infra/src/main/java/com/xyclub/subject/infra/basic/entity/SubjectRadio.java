package com.xyclub.subject.infra.basic.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author lxy
 * @date 2026/07/14 14:38
 **/
@Data
public class SubjectRadio {
    private static final long serialVersionUID = 528349687787614869L;
    /**
     * 主键
     */
    private Long id;
    /**
     * 题目id
     */
    private Long subjectId;
    /**
     * a,b,c,d
     */
    private Integer optionType;
    /**
     * 选项内容
     */
    private String optionContent;
    /**
     * 是否正确
     */
    private Integer isCorrect;
    /**
     * 创建人
     */
    private String createdBy;
    /**
     * 创建时间
     */
    private Date createdTime;
    /**
     * 修改人
     */
    private String updateBy;
    /**
     * 修改时间
     */
    private Date updateTime;

    private Integer isDeleted;

}
