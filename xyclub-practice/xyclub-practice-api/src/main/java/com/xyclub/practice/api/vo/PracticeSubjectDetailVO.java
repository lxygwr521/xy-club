package com.xyclub.practice.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 练习题目明细 VO
 */
@Data
public class PracticeSubjectDetailVO implements Serializable {

    /**
     * 题目 id
     */
    private Long subjectId;

    /**
     * 题目类型（1单选 2多选 3判断 4简答）
     */
    private Integer subjectType;

    /**
     * 是否已回答：1 已答 0 未答（继续练习时用于恢复进度）
     */
    private Integer isAnswer;

}
