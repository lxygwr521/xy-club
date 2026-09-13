package com.xyclub.practice.api.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询练习评估报告的请求参数。
 */
@Data
public class GetReportReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 练习 id
     */
    private Long practiceId;

}
