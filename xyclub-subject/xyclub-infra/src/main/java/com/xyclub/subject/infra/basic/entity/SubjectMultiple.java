package com.xyclub.subject.infra.basic.entity;
import lombok.Data;
import java.util.Date;
/**
 * @author lxy
 * @date 2026/07/14 14:37
 **/
@Data
public class SubjectMultiple {
    private static final long serialVersionUID = 575755837160743772L;
    /**
     * 主键
     */
    private Long id;
    /**
     * 题目id
     */
    private Long subjectId;
    /**
     * 选项类型
     */
    private Long optionType;
    /**
     * 选项内容
     */
    private String optionContent;
    /**
     * 是否正确
     */
    private Integer isCorrect;
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
     * 更新时间
     */
    private Date updateTime;

    private Integer isDeleted;


}
