package com.xyclub.practice.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 评估报告中的单项技能掌握情况。
 */
@Data
public class ReportSkillVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前标签下题目的正确率
     */
    private BigDecimal star;

    /**
     * 标签名称
     */
    private String name;

}
