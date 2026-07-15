package com.xyclub.subject.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lxy
 * @date 2026/07/14 13:59
 **/
@Data
public class SubjectAnswerBO implements Serializable {
    /**
     * 答案选项标识
     */
    private Integer optionType;

    /**
     * 答案
     */
    private String optionContent;

    /**
     * 是否正确
     */
    private Integer isCorrect;

}
