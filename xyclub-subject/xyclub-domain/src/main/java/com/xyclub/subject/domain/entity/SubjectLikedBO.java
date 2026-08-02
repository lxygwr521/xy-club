package com.xyclub.subject.domain.entity;

import com.xyclub.subject.common.entity.PageInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 题目点赞 BO。
 */
@Data
public class SubjectLikedBO extends PageInfo implements Serializable {

    /**
     * 主键。
     */
    private Long id;

    /**
     * 题目 id。
     */
    private Long subjectId;

    /**
     * 点赞人 id。
     */
    private String likeUserId;

    /**
     * 点赞状态：1 点赞，0 取消点赞。
     */
    private Integer status;

    /**
     * 创建人。
     */
    private String createdBy;

    /**
     * 创建时间。
     */
    private Date createdTime;

    /**
     * 修改人。
     */
    private String updateBy;

    /**
     * 修改时间。
     */
    private Date updateTime;

    /**
     * 删除标识。
     */
    private Integer isDeleted;

}
