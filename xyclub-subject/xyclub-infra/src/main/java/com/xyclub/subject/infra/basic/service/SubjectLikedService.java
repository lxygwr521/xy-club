package com.xyclub.subject.infra.basic.service;

import com.xyclub.subject.infra.basic.entity.SubjectLiked;

import java.util.List;

/**
 * 题目点赞表基础服务。
 */
public interface SubjectLikedService {

    /**
     * 根据主键查询点赞记录。
     */
    SubjectLiked queryById(Long id);

    /**
     * 新增点赞记录。
     */
    int insert(SubjectLiked subjectLiked);

    /**
     * 更新点赞记录。
     */
    int update(SubjectLiked subjectLiked);

    /**
     * 根据主键物理删除点赞记录。
     */
    boolean deleteById(Long id);

    /**
     * 根据非空字段查询单条点赞记录。
     */
    SubjectLiked queryByCondition(SubjectLiked subjectLiked);

    void batchInsert(List<SubjectLiked> subjectLikedList);

}
