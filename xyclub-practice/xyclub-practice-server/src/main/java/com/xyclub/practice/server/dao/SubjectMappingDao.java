package com.xyclub.practice.server.dao;


import com.xyclub.practice.server.entity.po.LabelCountPO;
import com.xyclub.practice.server.entity.po.SubjectMappingPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目分类关系表(SubjectMapping)表数据库访问层
 *
 * @author makejava
 * @since 2023-10-03 22:17:07
 */
public interface SubjectMappingDao {

    /**
     * 统计指定分类下各标签的题目数量（仅统计指定题型）
     *
     * @param categoryId      分类ID
     * @param subjectTypeList 题目类型列表
     * @return 标签题目数量统计列表
     */
    List<LabelCountPO> getLabelSubjectCount(@Param("categoryId") Long categoryId,
                                            @Param("subjectTypeList") List<Integer> subjectTypeList);

    /**
     * 查询题目关联的全部有效标签 id。
     *
     * @param subjectId 题目 id
     * @return 题目和标签的关联记录
     */
    List<SubjectMappingPO> getLabelIdsBySubjectId(Long subjectId);

}
