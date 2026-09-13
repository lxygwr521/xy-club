package com.xyclub.practice.server.entity.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 题目与分类、标签的关联关系。
 */
@Data
public class SubjectMappingPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 题目 id */
    private Long subjectId;

    /** 分类 id */
    private Long categoryId;

    /** 标签 id */
    private Long labelId;

    /** 创建人 */
    private String createdBy;

    /** 创建时间 */
    private Date createdTime;

    /** 修改人 */
    private String updateBy;

    /** 修改时间 */
    private Date updateTime;

    /** 删除标识：0-未删除，1-已删除 */
    private Integer isDeleted;

}
