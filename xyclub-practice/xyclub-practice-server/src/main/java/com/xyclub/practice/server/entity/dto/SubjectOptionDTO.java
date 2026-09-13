package com.xyclub.practice.server.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 判题使用的题目选项。
 */
@Data
public class SubjectOptionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 选项编号，例如 1、2、3、4 */
    private Integer optionType;

    /** 选项内容 */
    private String optionContent;

    /** 是否为正确答案：1 是、0 否 */
    private Integer isCorrect;
}
