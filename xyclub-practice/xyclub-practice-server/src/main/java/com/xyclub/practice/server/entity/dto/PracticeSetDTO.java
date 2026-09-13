package com.xyclub.practice.server.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 套题查询条件。
 */
@Data
public class PracticeSetDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 排除的套题 id */
    private List<Long> excludeSetId;

    /** 套题类型 */
    private Integer setType;

    /** 一级分类 id */
    private Long primaryCategoryId;

    /** 查询数量 */
    private Integer limitCount;

    /** 排序类型：1-默认，2-最新，3-最热 */
    private Integer orderType;

    /** 套题名称 */
    private String setName;

}
