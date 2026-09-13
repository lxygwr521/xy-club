package com.xyclub.practice.api.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 获取练习题详情请求参数
 */
@Data
public class GetPracticeSubjectReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 题目 id */
    private Long subjectId;

    /** 题目类型 */
    private Integer subjectType;
}
