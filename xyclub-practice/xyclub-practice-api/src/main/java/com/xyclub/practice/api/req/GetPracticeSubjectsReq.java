package com.xyclub.practice.api.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 获取练习题列表请求参数
 */
@Data
public class GetPracticeSubjectsReq implements Serializable {

    /**
     * 套题 id
     */
    private Long setId;

    /**
     * 练习 id（继续上次练习时传入；首次进入为空）
     */
    private Long practiceId;

}
