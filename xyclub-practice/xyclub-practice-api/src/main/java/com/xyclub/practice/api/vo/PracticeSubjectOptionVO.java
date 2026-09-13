package com.xyclub.practice.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 练习题选项
 */
@Data
public class PracticeSubjectOptionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 答案类型 */
    private Integer optionType;

    /** 答案内容 */
    private String optionContent;

    /** 是否正确，1 是，0 否 */
    private Integer isCorrect;
}
