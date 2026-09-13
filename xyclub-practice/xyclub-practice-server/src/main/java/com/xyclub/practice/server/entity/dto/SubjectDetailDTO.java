package com.xyclub.practice.server.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 题目及标准答案详情。
 * Service 使用该对象统一承载不同题型的答案信息，供判题流程消费。
 */
@Data
public class SubjectDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 题目 id */
    private Long id;

    /** 题目名称 */
    private String subjectName;

    /** 判断题标准答案：1 正确、0 错误 */
    private Integer isCorrect;

    /** 题目解析 */
    private String subjectParse;

    /** 单选题或多选题的选项与正确标识 */
    private List<SubjectOptionDTO> optionList;
}
