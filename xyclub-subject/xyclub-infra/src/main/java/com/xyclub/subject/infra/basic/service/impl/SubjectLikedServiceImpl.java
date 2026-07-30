package com.xyclub.subject.infra.basic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xyclub.subject.infra.basic.dao.SubjectLikedDao;
import com.xyclub.subject.infra.basic.entity.SubjectLiked;
import com.xyclub.subject.infra.basic.service.SubjectLikedService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 题目点赞表基础服务实现。
 */
@Service("subjectLikedService")
public class SubjectLikedServiceImpl implements SubjectLikedService {

    @Resource
    private SubjectLikedDao subjectLikedDao;

    /**
     * 根据主键查询点赞记录。
     */
    @Override
    public SubjectLiked queryById(Long id) {
        return this.subjectLikedDao.selectById(id);
    }

    /**
     * 插入点赞记录。
     */
    @Override
    public int insert(SubjectLiked subjectLiked) {
        return this.subjectLikedDao.insert(subjectLiked);
    }

    /**
     * 按主键更新点赞记录。
     */
    @Override
    public int update(SubjectLiked subjectLiked) {
        return this.subjectLikedDao.updateById(subjectLiked);
    }

    /**
     * 根据主键物理删除点赞记录。
     */
    @Override
    public boolean deleteById(Long id) {
        return this.subjectLikedDao.deleteById(id) > 0;
    }

    /**
     * 将非空字段拼成等值条件，查询唯一点赞记录。
     */
    @Override
    public SubjectLiked queryByCondition(SubjectLiked subjectLiked) {
        LambdaQueryWrapper<SubjectLiked> queryWrapper = Wrappers.<SubjectLiked>lambdaQuery()
                .eq(Objects.nonNull(subjectLiked.getId()), SubjectLiked::getId, subjectLiked.getId())
                .eq(Objects.nonNull(subjectLiked.getSubjectId()), SubjectLiked::getSubjectId,
                        subjectLiked.getSubjectId())
                .eq(Objects.nonNull(subjectLiked.getLikeUserId()), SubjectLiked::getLikeUserId,
                        subjectLiked.getLikeUserId())
                .eq(Objects.nonNull(subjectLiked.getStatus()), SubjectLiked::getStatus, subjectLiked.getStatus())
                .eq(Objects.nonNull(subjectLiked.getCreatedBy()), SubjectLiked::getCreatedBy,
                        subjectLiked.getCreatedBy())
                .eq(Objects.nonNull(subjectLiked.getCreatedTime()), SubjectLiked::getCreatedTime,
                        subjectLiked.getCreatedTime())
                .eq(Objects.nonNull(subjectLiked.getUpdateBy()), SubjectLiked::getUpdateBy,
                        subjectLiked.getUpdateBy())
                .eq(Objects.nonNull(subjectLiked.getUpdateTime()), SubjectLiked::getUpdateTime,
                        subjectLiked.getUpdateTime())
                .eq(Objects.nonNull(subjectLiked.getIsDeleted()), SubjectLiked::getIsDeleted, subjectLiked.getIsDeleted());
        return subjectLikedDao.selectOne(queryWrapper);
    }

    @Override
    public void batchInsert(List<SubjectLiked> subjectLikedList) {
        this.subjectLikedDao.insertBatch(subjectLikedList);
    }

}
