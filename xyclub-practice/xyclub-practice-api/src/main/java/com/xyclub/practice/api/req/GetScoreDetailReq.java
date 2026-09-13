package com.xyclub.practice.api.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询练习每题得分明细的请求参数。
 */
@Data
public class GetScoreDetailReq implements Serializable {

    /**
     * 练习 id
     */
    private Long practiceId;

}
