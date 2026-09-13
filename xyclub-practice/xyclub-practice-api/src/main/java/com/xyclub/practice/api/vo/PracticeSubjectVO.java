package com.xyclub.practice.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 练习题详情
 */
@Data
public class PracticeSubjectVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 题目名称 */
    private String subjectName;

    /** 题目类型 */
    private Integer subjectType;

    /** 简答题答案内容 */
    private List<String> answerContentList;

    /** 单选、多选、判断题选项 */
    private List<PracticeSubjectOptionVO> optionList;
}
