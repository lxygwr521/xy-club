package com.xyclub.practice.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 专项练习内容 VO（一级分类维度）
 */
@Data
public class SpecialPracticeVO implements Serializable {

    /**
     * 一级分类名称
     */
    private String primaryCategoryName;

    /**
     * 一级分类ID
     */
    private Long primaryCategoryId;

    /**
     * 二级分类列表
     */
    private List<SpecialPracticeCategoryVO> categoryList;

}
