package com.xyclub.practice.server.entity.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 套题信息表(PracticeSet)实体
 */
@Data
public class PracticeSetPO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 套题名称
     */
    private String setName;

    /**
     * 套题类型：1 实时生成 2 预设套题
     */
    private Integer setType;

    /**
     * 套题热度
     */
    private Integer setHeat;

    /**
     * 套题描述
     */
    private String setDesc;

    /**
     * 大类 id（一级分类）
     */
    private Long primaryCategoryId;

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
