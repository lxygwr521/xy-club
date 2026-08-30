package com.xyclub.practice.server.entity.po;

import lombok.Data;

/**
 * 题目分类 PO
 */
@Data
public class CategoryPO {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类类型（1-一级分类 2-二级分类）
     */
    private Integer categoryType;

    /**
     * 父分类ID
     */
    private Long parentId;

}
