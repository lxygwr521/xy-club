package com.xyclub.practice.server.service;

import com.xyclub.practice.api.vo.SpecialPracticeVO;

import java.util.List;

/**
 * 练习套卷服务接口
 */
public interface PracticeSetService {

    /**
     * 获取专项练习内容
     */
    List<SpecialPracticeVO> getSpecialPracticeContent();

}
