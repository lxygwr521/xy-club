package com.xyclub.subject.domain.service;

import com.xyclub.subject.domain.entity.SubjectLikedBO;

/**
 * 题目点赞领域服务。
 */
public interface SubjectLikedDomainService {

    /**
     * 添加或取消题目点赞。
     */
    void add(SubjectLikedBO subjectLikedBO);

    /**
     * 判断当前用户是否点赞过题目。
     */
    Boolean isLiked(String subjectId, String userId);

    /**
     * 查询题目点赞数量。
     */
    Integer getLikedCount(String subjectId);

    /**
     * 更新题目点赞记录。
     */
    Boolean update(SubjectLikedBO subjectLikedBO);

    /**
     * 逻辑删除题目点赞记录。
     */
    Boolean delete(SubjectLikedBO subjectLikedBO);

    /**
     * 同步点赞数据。
     */
    void syncLiked();

}
