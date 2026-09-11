package com.xyclub.practice.server.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 练习题目查询条件 DTO
 */
@Data
public class PracticeSubjectDTO implements Serializable {

    /**
     * 分类与标签组合的 ids（格式：分类ID-标签ID）
     */
    private List<String> assembleIds;

    /**
     * 题目类型（1单选 2多选 3判断 4简答）
     */
    private Integer subjectType;

    /**
     * 需要的题目数量
     */
    private Integer subjectCount;

    /**
     * 要排除的题目 id（避免重复抽题）
     */
    private List<Long> excludeSubjectIds;


}
