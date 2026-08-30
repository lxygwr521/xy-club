package com.xyclub.practice.server.dao;


import com.xyclub.practice.server.entity.dto.CategoryDTO;
import com.xyclub.practice.server.entity.po.CategoryPO;
import com.xyclub.practice.server.entity.po.PrimaryCategoryPO;

import java.util.List;

/**
 * 题目分类(SubjectCategory)表数据库访问层
 *
 * @author makejava
 * @since 2023-10-01 21:49:58
 */
public interface SubjectCategoryDao {

    /**
     * 查询包含题目的所有一级分类（按父分类ID分组统计题目数）
     *
     * @param categoryDTO 查询条件（含题目类型列表）
     * @return 一级分类列表
     */
    List<PrimaryCategoryPO> getPrimaryCategory(CategoryDTO categoryDTO);

    /**
     * 根据主键查询分类
     *
     * @param id 分类ID
     * @return 分类信息
     */
    CategoryPO selectById(Long id);

    /**
     * 按条件查询分类列表（父分类ID + 分类类型）
     *
     * @param categoryDTOTemp 查询条件
     * @return 分类列表
     */
    List<CategoryPO> selectList(CategoryDTO categoryDTOTemp);

}
