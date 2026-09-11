package com.xyclub.practice.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 练习套卷 VO（开始练习后返回）
 */
@Data
public class PracticeSetVO implements Serializable {

    /**
     * 套题 id
     */
    private Long setId;

    /**
     * 套题名称
     */
    private String setName;

    /**
     * 套题热度
     */
    private Integer setHeat;

    /**
     * 套题描述
     */
    private String setDesc;

}
