package com.xyclub.subject.infra.basic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 题目点赞表实体。
 */
@Data
@TableName("subject_liked")
public class SubjectLiked implements Serializable {

    /**
     * 主键。
     */
    @TableId(value = "`id`", type = IdType.AUTO)
    private Long id;

    /**
     * 题目 id。
     */
    @TableField("`subject_id`")
    private Long subjectId;

    /**
     * 点赞人 id。
     */
    @TableField("`like_user_id`")
    private String likeUserId;

    /**
     * 点赞状态：1 点赞，0 取消点赞。
     */
    @TableField("`status`")
    private Integer status;

    /**
     * 创建人。
     */
    @TableField("`created_by`")
    private String createdBy;

    /**
     * 创建时间。
     */
    @TableField("`created_time`")
    private Date createdTime;

    /**
     * 修改人。
     */
    @TableField("`update_by`")
    private String updateBy;

    /**
     * 修改时间。
     */
    @TableField("`update_time`")
    private Date updateTime;

    /**
     * 删除标识。
     */
    @TableField("`is_deleted`")
    private Integer isDeleted;

}
