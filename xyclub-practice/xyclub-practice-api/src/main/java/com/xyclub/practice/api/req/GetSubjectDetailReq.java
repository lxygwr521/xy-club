package com.xyclub.practice.api.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询答案解析中单题详情的请求参数。
 */
@Data
public class GetSubjectDetailReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 练习 id，用于定位用户本次练习的作答记录
     */
    private Long practiceId;

    /**
     * 题目 id
     */
    private Long subjectId;

    /**
     * 题目类型
     */
    private Integer subjectType;

}
