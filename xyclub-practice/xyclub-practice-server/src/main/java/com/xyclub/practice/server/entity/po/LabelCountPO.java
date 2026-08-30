package com.xyclub.practice.server.entity.po;

import lombok.Data;

/**
 * 标签题目数量统计 PO
 */
@Data
public class LabelCountPO {

    /**
     * 标签ID
     */
    private Long labelId;

    /**
     * 该标签下符合条件的题目数量
     */
    private Integer count;

    /**
     * 标签名称
     */
    private String labelName;

}
