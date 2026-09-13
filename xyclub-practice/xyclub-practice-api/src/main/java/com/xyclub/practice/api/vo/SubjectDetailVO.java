package com.xyclub.practice.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 答案解析页面的单题详情。
 */
@Data
public class SubjectDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 正确答案
     */
    private List<Integer> correctAnswer;

    /**
     * 用户在本次练习中提交的答案
     */
    private List<Integer> respondAnswer;

    /**
     * 题目解析
     */
    private String subjectParse;

    /**
     * 选项详情
     */
    private List<PracticeSubjectOptionVO> optionList;

    /**
     * 题目关联的标签名称
     */
    private List<String> labelNames;

    /**
     * 题目名称
     */
    private String subjectName;

}
