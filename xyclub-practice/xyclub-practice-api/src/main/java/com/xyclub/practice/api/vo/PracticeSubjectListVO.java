package com.xyclub.practice.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 练习题目列表 VO
 */
@Data
public class PracticeSubjectListVO implements Serializable {

    /**
     * 练习标题（套题名称）
     */
    private String title;

    /**
     * 题目列表
     */
    private List<PracticeSubjectDetailVO> subjectList;

    /**
     * 练习 id（首次进入时由后端创建并返回，用于后续答题/交卷）
     */
    private Long practiceId;

    /**
     * 已用时（继续练习时返回，用于恢复计时器）
     */
    private String timeUse;

}
