package com.xyclub.practice.server.entity.dto;

import lombok.Data;

import java.util.List;

/**
 * 分类查询条件 DTO
 */
@Data
public class CategoryDTO {

    /**
     * 题目类型列表（1-单选 2-多选 3-判断）
     */
    private List<Integer> subjectTypeList;

    /**
     * 分类类型（1-一级分类 2-二级分类）
     */
    private Integer categoryType;

    /**
     * 父分类ID
     */
    private Long parentId;

}
