package com.xyclub.practice.server.dao;

import com.xyclub.practice.server.entity.po.SubjectLabelPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目标签表(SubjectLabel)表数据库访问层
 *
 * @author makejava
 * @since 2023-10-03 21:50:29
 */
public interface SubjectLabelDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    SubjectLabelPO queryById(Long id);

    /**
     * 根据标签 id 集合批量查询标签名称。
     *
     * @param labelIds 标签 id 集合
     * @return 标签名称集合
     */
    List<String> getLabelNameByIds(@Param("labelIds") List<Long> labelIds);

}
