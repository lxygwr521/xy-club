package com.xyclub.practice.api.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 提交练题情况请求参数
 */
@Data
public class SubmitPracticeDetailReq implements Serializable {

    /**
     * 套题 id
     */
    private Long setId;

    /**
     * 练习 id
     */
    private Long practiceId;

    /**
     * 用时（前端计时器格式，如 HHmmss）
     */
    private String timeUse;

    /**
     * 交卷时间（yyyy-MM-dd HH:mm:ss）
     */
    private String submitTime;


}
