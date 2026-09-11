package com.xyclub.practice.server.service;

import com.xyclub.practice.api.req.GetPracticeSubjectsReq;
import com.xyclub.practice.api.vo.SpecialPracticeVO;
import com.xyclub.practice.api.vo.PracticeSetVO;
import com.xyclub.practice.api.vo.PracticeSubjectListVO;
import com.xyclub.practice.server.entity.dto.PracticeSubjectDTO;

import java.util.List;

/**
 * 练习套卷服务接口
 */
public interface PracticeSetService {

    /**
     * 获取专项练习内容
     */
    List<SpecialPracticeVO> getSpecialPracticeContent();

    /**
     * 开始练习：按分类标签组合随机组卷并生成练习套题
     *
     * @param dto 练习题目查询条件
     * @return 生成的套题信息
     */
    PracticeSetVO addPractice(PracticeSubjectDTO dto);

    /**
     * 获取练习题目列表
     *
     * @param req 请求参数（套题 id）
     * @return 练习标题与题目列表
     */
    PracticeSubjectListVO getSubjects(GetPracticeSubjectsReq req);

}
