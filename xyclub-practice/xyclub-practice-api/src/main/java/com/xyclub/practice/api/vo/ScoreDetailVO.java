package com.xyclub.practice.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 单道题目的得分结果。
 */
@Data
public class ScoreDetailVO implements Serializable {

    /**
     * 题目 id
     */
    private Long subjectId;

    /**
     * 题目类型
     */
    private Integer subjectType;

    /**
     * 是否答对：1-正确，0-错误
     */
    private Integer isCorrect;

}
