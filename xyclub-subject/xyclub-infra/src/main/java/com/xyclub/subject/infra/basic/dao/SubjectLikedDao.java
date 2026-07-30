package com.xyclub.subject.infra.basic.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xyclub.subject.infra.basic.entity.SubjectLiked;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 题目点赞表数据库访问层。
 */
@Repository
public interface SubjectLikedDao extends BaseMapper<SubjectLiked> {

    int insertBatch(@Param("entities") List<SubjectLiked> entities);

}
