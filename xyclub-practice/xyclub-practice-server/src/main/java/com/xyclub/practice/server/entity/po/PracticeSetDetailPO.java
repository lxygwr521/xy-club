package com.xyclub.practice.server.entity.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 套题内容表(PracticeSetDetail)实体
 */
@Data
public class PracticeSetDetailPO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 套题 id
     */
    private Long setId;

    /**
     * 题目 id
     */
    private Long subjectId;

    /**
     * 题目类型（1单选 2多选 3判断 4简答）
     */
    private Integer subjectType;

    /**
     * 创建人
     */
    private String createdBy;
    /**
     * 创建时间
     */
    private Date createdTime;
    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 删除标识（0-未删除 1-已删除）
     */
    private Integer isDeleted;
    /**
     * 更新时间
     */
    private Date updateTime;

}
