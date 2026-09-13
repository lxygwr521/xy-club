package com.xyclub.practice.server.entity.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 判断题答案实体，对应 subject_judge 表。
 */
@Data
public class SubjectJudgePO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 题目 id */
    private Long subjectId;

    /** 标准答案：1 正确、0 错误 */
    private Integer isCorrect;

    /** 创建人 */
    private String createdBy;

    /** 创建时间 */
    private Date createdTime;

    /** 更新人 */
    private String updateBy;

    /** 更新时间 */
    private Date updateTime;

    /** 删除标识：0 未删除、1 已删除 */
    private Integer isDeleted;
}
