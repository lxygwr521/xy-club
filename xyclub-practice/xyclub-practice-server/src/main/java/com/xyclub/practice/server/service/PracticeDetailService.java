package com.xyclub.practice.server.service;

import com.xyclub.practice.api.req.SubmitPracticeDetailReq;

/**
 * 练习详情服务接口
 */
public interface PracticeDetailService {

    /**
     * 提交练题情况（交卷）
     *
     * @param req 提交参数
     * @return 是否提交成功
     */
    Boolean submit(SubmitPracticeDetailReq req);


}
