package com.xyclub.practice.server.service;

import com.xyclub.practice.api.req.GetScoreDetailReq;
import com.xyclub.practice.api.req.GetSubjectDetailReq;
import com.xyclub.practice.api.req.SubmitPracticeDetailReq;
import com.xyclub.practice.api.req.SubmitSubjectDetailReq;
import com.xyclub.practice.api.vo.ScoreDetailVO;
import com.xyclub.practice.api.vo.SubjectDetailVO;

import java.util.List;

/**
 * 练习详情服务接口
 */
public interface PracticeDetailService {

    /**
     * 保存单道题目的作答结果。
     * Service 会查询标准答案完成判题，并按“练习 + 题目 + 用户”新增或覆盖作答记录。
     *
     * @param req 当前题目答案和累计用时
     * @return 是否保存成功
     */
    Boolean submitSubject(SubmitSubjectDetailReq req);

    /**
     * 提交练题情况（交卷）
     *
     * @param req 提交参数
     * @return 是否提交成功
     */
    Boolean submit(SubmitPracticeDetailReq req);

    /**
     * 查询指定练习中每道题的得分结果。
     *
     * @param req 练习 id
     * @return 每道题的题目 id、题型和正确状态
     */
    List<ScoreDetailVO> getScoreDetail(GetScoreDetailReq req);

    /**
     * 查询答案解析页面的单题详情。
     *
     * @param req 练习、题目和题型信息
     * @return 题目内容、标准答案、用户答案、选项和标签
     */
    SubjectDetailVO getSubjectDetail(GetSubjectDetailReq req);

}
