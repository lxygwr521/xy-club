package com.xyclub.practice.api.req;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 单道练习题提交请求。
 * 前端切换下一题或交卷前调用，用于保存当前题目的答案和累计用时。
 */
@Data
public class SubmitSubjectDetailReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前练习记录 id */
    private Long practiceId;

    /** 当前题目 id */
    private Long subjectId;

    /**
     * 用户答案。
     * 单选、判断题通常只有一个值，多选题可以包含多个选项编号。
     */
    private List<Integer> answerContents;

    /** 题目类型：1 单选、2 多选、3 判断 */
    private Integer subjectType;

    /** 当前练习累计用时，前端格式为 HHmmss */
    private String timeUse;
}
