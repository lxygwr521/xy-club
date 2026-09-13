package com.xyclub.practice.server.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 题目详情查询条件。
 * 用于在提交答案时按题目 id 和题型查询标准答案。
 */
@Data
public class SubjectDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 通用主键，保留给题目详情扩展场景 */
    private Long id;

    /** 题目 id */
    private Long subjectId;

    /** 题目名称 */
    private String subjectName;

    /** 题目类型：1 单选、2 多选、3 判断、4 简答 */
    private Integer subjectType;
}
