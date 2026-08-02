package com.xyclub.subject.application.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.xyclub.subject.application.convert.SubjectLikedDTOConverter;
import com.xyclub.subject.application.dto.SubjectLikedDTO;
import com.xyclub.subject.common.entity.PageResult;
import com.xyclub.subject.common.entity.Result;
import com.xyclub.subject.common.util.LoginUtil;
import com.xyclub.subject.domain.entity.SubjectLikedBO;
import com.xyclub.subject.domain.service.SubjectLikedDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 题目点赞接口。
 */
@RestController
@RequestMapping("/subjectLiked/")
@Slf4j
public class SubjectLikedController {

    @Resource
    private SubjectLikedDomainService subjectLikedDomainService;

    /**
     * 添加或取消题目点赞，点赞人取当前登录用户。
     */
    @RequestMapping("add")
    public Result<Boolean> add(@RequestBody SubjectLikedDTO subjectLikedDTO) {
        try {
            if (log.isInfoEnabled()) {
                log.info("SubjectLikedController.add.dto:{}", JSON.toJSONString(subjectLikedDTO));
            }
            Preconditions.checkNotNull(subjectLikedDTO.getSubjectId(), "题目id不能为空");
            Preconditions.checkNotNull(subjectLikedDTO.getStatus(), "点赞状态不能为空");
            subjectLikedDTO.setLikeUserId(LoginUtil.getLoginId());
            Preconditions.checkNotNull(subjectLikedDTO.getLikeUserId(), "点赞人不能为空");
            SubjectLikedBO subjectLikedBO = SubjectLikedDTOConverter.INSTANCE.convertDTOToBO(subjectLikedDTO);
            subjectLikedDomainService.add(subjectLikedBO);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("SubjectLikedController.add.error:{}", e.getMessage(), e);
            return Result.fail("新增题目点赞失败");
        }
    }

    /**
     * 分页查询当前用户点赞过的题目。
     */
    @PostMapping("/getSubjectLikedPage")
    public Result<PageResult<SubjectLikedDTO>> getSubjectLikedPage(@RequestBody SubjectLikedDTO subjectLikedDTO) {
        try {
            if (log.isInfoEnabled()) {
                log.info("SubjectLikedController.getSubjectLikedPage.dto:{}", JSON.toJSONString(subjectLikedDTO));
            }
            SubjectLikedBO subjectLikedBO = SubjectLikedDTOConverter.INSTANCE.convertDTOToBO(subjectLikedDTO);
            subjectLikedBO.setPageNo(subjectLikedDTO.getPageNo());
            subjectLikedBO.setPageSize(subjectLikedDTO.getPageSize());
            PageResult<SubjectLikedBO> boPageResult = subjectLikedDomainService.getSubjectLikedPage(subjectLikedBO);
            return Result.ok(boPageResult);
        } catch (Exception e) {
            log.error("SubjectLikedController.getSubjectLikedPage.error:{}", e.getMessage(), e);
            return Result.fail("分页查询我的点赞失败");
        }
    }

    /**
     * 修改题目点赞记录。
     */
    @RequestMapping("update")
    public Result<Boolean> update(@RequestBody SubjectLikedDTO subjectLikedDTO) {
        try {
            if (log.isInfoEnabled()) {
                log.info("SubjectLikedController.update.dto:{}", JSON.toJSONString(subjectLikedDTO));
            }
            checkSubjectLiked(subjectLikedDTO);
            SubjectLikedBO subjectLikedBO = SubjectLikedDTOConverter.INSTANCE.convertDTOToBO(subjectLikedDTO);
            return Result.ok(subjectLikedDomainService.update(subjectLikedBO));
        } catch (Exception e) {
            log.error("SubjectLikedController.update.error:{}", e.getMessage(), e);
            return Result.fail("更新题目点赞失败");
        }
    }

    /**
     * 删除题目点赞记录。
     */
    @RequestMapping("delete")
    public Result<Boolean> delete(@RequestBody SubjectLikedDTO subjectLikedDTO) {
        try {
            if (log.isInfoEnabled()) {
                log.info("SubjectLikedController.delete.dto:{}", JSON.toJSONString(subjectLikedDTO));
            }
            checkSubjectLiked(subjectLikedDTO);
            SubjectLikedBO subjectLikedBO = SubjectLikedDTOConverter.INSTANCE.convertDTOToBO(subjectLikedDTO);
            return Result.ok(subjectLikedDomainService.delete(subjectLikedBO));
        } catch (Exception e) {
            log.error("SubjectLikedController.delete.error:{}", e.getMessage(), e);
            return Result.fail("删除题目点赞失败");
        }
    }

    /**
     * 校验点赞基础参数。
     */
    private void checkSubjectLiked(SubjectLikedDTO subjectLikedDTO) {
        Preconditions.checkNotNull(subjectLikedDTO.getId(), "主键不能为空");
        Preconditions.checkNotNull(subjectLikedDTO.getSubjectId(), "题目id不能为空");
        Preconditions.checkNotNull(subjectLikedDTO.getLikeUserId(), "点赞人id不能为空");
        Preconditions.checkNotNull(subjectLikedDTO.getStatus(), "点赞状态不能为空");
        Preconditions.checkNotNull(subjectLikedDTO.getCreatedBy(), "创建人不能为空");
        Preconditions.checkNotNull(subjectLikedDTO.getCreatedTime(), "创建时间不能为空");
        Preconditions.checkNotNull(subjectLikedDTO.getUpdateBy(), "修改人不能为空");
        Preconditions.checkNotNull(subjectLikedDTO.getUpdateTime(), "修改时间不能为空");
        Preconditions.checkNotNull(subjectLikedDTO.getIsDeleted(), "删除标识不能为空");
    }

}
