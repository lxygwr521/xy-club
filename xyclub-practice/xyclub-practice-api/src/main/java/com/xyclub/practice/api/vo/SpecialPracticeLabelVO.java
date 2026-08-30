package com.xyclub.practice.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 专项练习标签 VO
 */
@Data
public class SpecialPracticeLabelVO implements Serializable {

    /**
     * 标签ID
     */
    private Long id;

    /**
     * 分类id-标签ID 组合标识（用于前端定位具体标签）
     */
    private String assembleId;

    /**
     * 标签名称
     */
    private String labelName;

}
