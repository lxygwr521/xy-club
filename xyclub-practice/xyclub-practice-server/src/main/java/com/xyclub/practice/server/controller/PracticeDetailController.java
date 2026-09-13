package com.xyclub.practice.server.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.xyclub.practice.api.common.Result;
import com.xyclub.practice.api.enums.SubjectInfoTypeEnum;
import com.xyclub.practice.api.req.GetReportReq;
import com.xyclub.practice.api.req.GetScoreDetailReq;
import com.xyclub.practice.api.req.GetSubjectDetailReq;
import com.xyclub.practice.api.req.SubmitPracticeDetailReq;
import com.xyclub.practice.api.req.SubmitSubjectDetailReq;
import com.xyclub.practice.api.vo.ReportVO;
import com.xyclub.practice.api.vo.ScoreDetailVO;
import com.xyclub.practice.api.vo.SubjectDetailVO;
import com.xyclub.practice.server.service.PracticeDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 练习详情控制器
 */
@RestController
@Slf4j
@RequestMapping("/practice/detail")
public class PracticeDetailController {

    @Resource
    private PracticeDetailService practiceDetailService;

    /**
     * 提交单道题目的答案。
     * 流程：校验练习、题目、题型和用时参数，交给 Service 计算答案正确性，
     * 随后新增或更新该题的作答明细，并刷新练习累计用时。
     *
     * @param req 当前题目答案及练习进度
     * @return 是否保存成功；参数或处理异常时返回失败信息
     */
    @PostMapping(value = "/submitSubject")
    public Result<Boolean> submitSubject(@RequestBody SubmitSubjectDetailReq req) {
        try {
            if (log.isInfoEnabled()) {
                log.info("练习提交题目入参{}", JSON.toJSONString(req));
            }
            Preconditions.checkArgument(!Objects.isNull(req), "参数不能为空！");
            Preconditions.checkArgument(!Objects.isNull(req.getPracticeId()), "练习id不能为空！");
            Preconditions.checkArgument(!Objects.isNull(req.getSubjectId()), "题目id不能为空！");
            Preconditions.checkArgument(!Objects.isNull(req.getSubjectType()), "题目类型不能为空！");
            Preconditions.checkArgument(isSupportedSubjectType(req.getSubjectType()),
                    "仅支持单选、多选和判断题！");
            Preconditions.checkArgument(!StringUtils.isBlank(req.getTimeUse()), "用时不能为空！");
            Boolean result = practiceDetailService.submitSubject(req);
            if (log.isInfoEnabled()) {
                log.info("练习提交题目出参{}", result);
            }
            return Result.ok(result);
        } catch (IllegalArgumentException e) {
            log.error("参数异常！错误原因{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("练习提交题目异常！错误原因{}", e.getMessage(), e);
            return Result.fail("练习提交题目异常！");
        }
    }

    /**
     * 提交练题情况（交卷）
     *
     * @param req 提交参数（套题id、练习id、用时、交卷时间）
     * @return 是否提交成功；参数异常或系统异常时返回失败信息
     */
    @PostMapping(value = "/submit")
    public Result<Boolean> submit(@RequestBody SubmitPracticeDetailReq req) {
        try {
            if (log.isInfoEnabled()) {
                log.info("提交练题情况入参{}", JSON.toJSONString(req));
            }
            Preconditions.checkArgument(!Objects.isNull(req), "参数不能为空！");
            Preconditions.checkArgument(!Objects.isNull(req.getSetId()), "套题id不能为空！");
            Preconditions.checkArgument(!StringUtils.isBlank(req.getSubmitTime()), "交卷时间不能为空！");
            Preconditions.checkArgument(!StringUtils.isBlank(req.getTimeUse()), "用时不能为空！");
            Boolean result = practiceDetailService.submit(req);
            if (log.isInfoEnabled()) {
                log.info("提交练题情况出参{}", JSON.toJSONString(result));
            }
            return Result.ok(result);
        } catch (IllegalArgumentException e) {
            log.error("参数异常！错误原因{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("提交练题情况异常！错误原因{}", e.getMessage(), e);
            return Result.fail("提交练题情况异常！");
        }
    }

    /**
     * 查询一次练习中每道题的得分结果。
     * 流程：校验练习 id，查询该次练习的全部作答明细，并返回题目、题型和正确状态。
     *
     * @param req 练习 id
     * @return 每道题的答题结果
     */
    @PostMapping(value = "/getScoreDetail")
    public Result<List<ScoreDetailVO>> getScoreDetail(@RequestBody GetScoreDetailReq req) {
        try {
            if (log.isInfoEnabled()) {
                log.info("每题得分入参{}", JSON.toJSONString(req));
            }
            Preconditions.checkArgument(!Objects.isNull(req), "参数不能为空！");
            Preconditions.checkArgument(!Objects.isNull(req.getPracticeId()), "练习id不能为空！");
            List<ScoreDetailVO> list = practiceDetailService.getScoreDetail(req);
            if (log.isInfoEnabled()) {
                log.info("每题得分出参{}", JSON.toJSONString(list));
            }
            return Result.ok(list);
        } catch (IllegalArgumentException e) {
            log.error("参数异常！错误原因{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("每题得分异常！错误原因{}", e.getMessage(), e);
            return Result.fail("每题得分异常！");
        }
    }

    /**
     * 查询答案解析页面的单题详情。
     * 流程：校验题目参数，查询题干、选项、标准答案、用户答案及关联标签后统一返回。
     *
     * @param req 练习 id、题目 id 和题目类型
     * @return 当前题目的完整答案解析信息
     */
    @PostMapping(value = "/getSubjectDetail")
    public Result<SubjectDetailVO> getSubjectDetail(@RequestBody GetSubjectDetailReq req) {
        try {
            if (log.isInfoEnabled()) {
                log.info("答案详情入参{}", JSON.toJSONString(req));
            }
            Preconditions.checkArgument(!Objects.isNull(req), "参数不能为空！");
            Preconditions.checkArgument(!Objects.isNull(req.getSubjectId()), "题目id不能为空！");
            Preconditions.checkArgument(!Objects.isNull(req.getSubjectType()), "题目类型不能为空！");
            SubjectDetailVO subjectDetailVO = practiceDetailService.getSubjectDetail(req);
            if (log.isInfoEnabled()) {
                log.info("答案详情出参{}", JSON.toJSONString(subjectDetailVO));
            }
            return Result.ok(subjectDetailVO);
        } catch (IllegalArgumentException e) {
            log.error("参数异常！错误原因{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("答案详情异常！错误原因{}", e.getMessage(), e);
            return Result.fail("答案详情异常！");
        }
    }

    /**
     * 获取一次练习的评估报告。
     * 流程：校验练习 id，查询套题和作答记录，统计总体正确数及各标签正确率后返回。
     *
     * @param req 练习 id
     * @return 套题名称、正确题数和技能图谱
     */
    @PostMapping(value = "/getReport")
    public Result<ReportVO> getReport(@RequestBody GetReportReq req) {
        try {
            if (log.isInfoEnabled()) {
                log.info("获取评估报告入参{}", JSON.toJSONString(req));
            }
            Preconditions.checkArgument(!Objects.isNull(req), "参数不能为空！");
            Preconditions.checkArgument(!Objects.isNull(req.getPracticeId()), "练习id不能为空！");
            ReportVO reportVO = practiceDetailService.getReport(req);
            if (log.isInfoEnabled()) {
                log.info("获取评估报告出参{}", JSON.toJSONString(reportVO));
            }
            return Result.ok(reportVO);
        } catch (IllegalArgumentException e) {
            log.error("参数异常！错误原因{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("获取评估报告异常！错误原因{}", e.getMessage(), e);
            return Result.fail("获取评估报告异常！");
        }
    }

    /**
     * 当前逐题提交接口只处理客观题；简答题答案不是选项编号，需使用独立的数据结构和判题策略。
     */
    private boolean isSupportedSubjectType(Integer subjectType) {
        return Objects.equals(subjectType, SubjectInfoTypeEnum.RADIO.getCode())
                || Objects.equals(subjectType, SubjectInfoTypeEnum.MULTIPLE.getCode())
                || Objects.equals(subjectType, SubjectInfoTypeEnum.JUDGE.getCode());
    }

}
