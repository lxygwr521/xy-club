package com.xyclub.practice.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 练习评估报告。
 */
@Data
public class ReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 套题名称
     */
    private String title;

    /**
     * 正确题数，格式为“正确数/总题数”
     */
    private String correctSubject;

    /**
     * 按题目标签统计的技能正确率
     */
    private List<ReportSkillVO> skill;

}
