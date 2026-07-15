package com.xyclub.subject.common.entity;

import lombok.Data;

/**
 * 分页请求实体
 * @author lxy
 * @date 2026/07/14 17:25
 **/
@Data
public class PageInfo {
    private Integer pageNo = 1;

    private Integer pageSize = 20;

    public Integer getPageNo() {
        if (pageNo == null || pageNo < 1) {
            return 1;
        }
        return pageNo;
    }

    public Integer getPageSize() {
        if (pageSize == null || pageSize < 1 || pageSize > Integer.MAX_VALUE) {
            return 20;
        }
        return pageSize;
    }

}
