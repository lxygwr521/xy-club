package com.xyclub.subject.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.xyclub.subject.common.enums.IsDeletedFlagEnum;
import com.xyclub.subject.common.enums.SubjectLikedStatusEnum;
import com.xyclub.subject.domain.convert.SubjectLikedBOConverter;
import com.xyclub.subject.domain.entity.SubjectLikedBO;
import com.xyclub.subject.domain.redis.RedisUtil;
import com.xyclub.subject.domain.service.SubjectLikedDomainService;
import com.xyclub.subject.infra.basic.entity.SubjectLiked;
import com.xyclub.subject.infra.basic.service.SubjectLikedService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 题目点赞领域服务实现。
 */
@Service
@Slf4j
public class SubjectLikedDomainServiceImpl implements SubjectLikedDomainService {

    @Resource
    private SubjectLikedService subjectLikedService;

    @Resource
    private RedisUtil redisUtil;

    private static final String SUBJECT_LIKED_KEY = "subject.liked";

    private static final String SUBJECT_LIKED_COUNT_KEY = "subject.liked.count";

    private static final String SUBJECT_LIKED_DETAIL_KEY = "subject.liked.detail";

    /**
     * 点赞状态写入 Redis，并维护题目的点赞数量。
     */
    @Override
    public void add(SubjectLikedBO subjectLikedBO) {
        Long subjectId = subjectLikedBO.getSubjectId();
        String likeUserId = subjectLikedBO.getLikeUserId();
        Integer status = subjectLikedBO.getStatus();
        String hashKey = buildSubjectLikedKey(subjectId.toString(), likeUserId);

        redisUtil.putHash(SUBJECT_LIKED_KEY, hashKey, status);
        String detailKey = SUBJECT_LIKED_DETAIL_KEY + "." + subjectId + "." + likeUserId;
        String countKey = SUBJECT_LIKED_COUNT_KEY + "." + subjectId;
        if (SubjectLikedStatusEnum.LIKED.getCode() == status) {
            redisUtil.increment(countKey, 1);
            redisUtil.set(detailKey, "1");
            return;
        }

        Integer count = redisUtil.getInt(countKey);
        if (Objects.isNull(count) || count <= 0) {
            return;
        }
        redisUtil.increment(countKey, -1);
        redisUtil.del(detailKey);
    }

    /**
     * 根据 detail key 判断用户是否已点赞。
     */
    @Override
    public Boolean isLiked(String subjectId, String userId) {
        String detailKey = SUBJECT_LIKED_DETAIL_KEY + "." + subjectId + "." + userId;
        return redisUtil.exist(detailKey);
    }

    /**
     * 从 Redis 中读取题目点赞数量，空值按 0 处理。
     */
    @Override
    public Integer getLikedCount(String subjectId) {
        String countKey = SUBJECT_LIKED_COUNT_KEY + "." + subjectId;
        Integer count = redisUtil.getInt(countKey);
        if (Objects.isNull(count) || count <= 0) {
            return 0;
        }
        return count;
    }

    /**
     * 组合题目与用户维度，标识一条点赞关系。
     */
    private String buildSubjectLikedKey(String subjectId, String userId) {
        return subjectId + ":" + userId;
    }

    /**
     * 按主键更新点赞记录。
     */
    @Override
    public Boolean update(SubjectLikedBO subjectLikedBO) {
        SubjectLiked subjectLiked = SubjectLikedBOConverter.INSTANCE.convertBOToEntity(subjectLikedBO);
        return subjectLikedService.update(subjectLiked) > 0;
    }

    /**
     * 只更新删除标识，保留历史点赞数据。
     */
    @Override
    public Boolean delete(SubjectLikedBO subjectLikedBO) {
        SubjectLiked subjectLiked = new SubjectLiked();
        subjectLiked.setId(subjectLikedBO.getId());
        subjectLiked.setIsDeleted(IsDeletedFlagEnum.DELETED.getCode());
        return subjectLikedService.update(subjectLiked) > 0;
    }

    @Override
    public void syncLiked() {
        Map<Object, Object> subjectLikedMap = redisUtil.getHashAndDelete(SUBJECT_LIKED_KEY);
        if (log.isInfoEnabled()) {
            log.info("syncLiked.subjectLikedMap:{}", JSON.toJSONString(subjectLikedMap));
        }
        if (MapUtils.isEmpty(subjectLikedMap)) {
            return;
        }
        //批量同步到数据库
        List<SubjectLiked> subjectLikedList = new LinkedList<>();
        subjectLikedMap.forEach((key, val) -> {
            SubjectLiked subjectLiked = new SubjectLiked();
            String[] keyArr = key.toString().split(":");
            String subjectId = keyArr[0];
            String likedUser = keyArr[1];
            subjectLiked.setSubjectId(Long.valueOf(subjectId));
            subjectLiked.setLikeUserId(likedUser);
            subjectLiked.setStatus(Integer.valueOf(val.toString()));
            subjectLikedList.add(subjectLiked);
        });
        subjectLikedService.batchInsert(subjectLikedList);
    }

}
