package com.xyclub.practice.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 专项练习二级分类 VO
 */
@Data
public class SpecialPracticeCategoryVO implements Serializable {

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 该分类下的标签列表
     */
    private List<SpecialPracticeLabelVO> labelList;

}
