package com.xyclub.practice.server.entity.po;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 练习表(PracticeInfo)实体：记录一次练习的整体情况
 */
@Data
public class PracticePO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 套题 id
     */
    private Long setId;

    /**
     * 完成情况：1 完成 0 未完成
     */
    private Integer completeStatus;

    /**
     * 所用时间（HH:mm:ss）
     */
    private String timeUse;

    /**
     * 交卷时间
     */
    private Date submitTime;

    /**
     * 正确率
     */
    private BigDecimal correctRate;

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
