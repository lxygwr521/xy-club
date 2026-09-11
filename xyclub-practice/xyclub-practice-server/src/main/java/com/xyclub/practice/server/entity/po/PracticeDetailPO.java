package com.xyclub.practice.server.entity.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 练习详情表(PracticeDetail)实体：记录某次练习中每道题的作答情况
 */
@Data
public class PracticeDetailPO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 练题 id（对应 practice_info.id）
     */
    private Long practiceId;

    /**
     * 题目 id
     */
    private Long subjectId;

    /**
     * 题目类型（1单选 2多选 3判断 4简答）
     */
    private Integer subjectType;

    /**
     * 是否正确：1 正确 0 错误
     */
    private Integer answerStatus;

    /**
     * 答案内容
     */
    private String answerContent;

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
