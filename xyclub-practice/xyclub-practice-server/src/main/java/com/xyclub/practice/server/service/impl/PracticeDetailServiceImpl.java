package com.xyclub.practice.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.xyclub.practice.api.enums.CompleteStatusEnum;
import com.xyclub.practice.api.enums.IsDeletedFlagEnum;
import com.xyclub.practice.api.req.SubmitPracticeDetailReq;
import com.xyclub.practice.server.dao.PracticeDao;
import com.xyclub.practice.server.dao.PracticeDetailDao;
import com.xyclub.practice.server.dao.PracticeSetDao;
import com.xyclub.practice.server.dao.PracticeSetDetailDao;
import com.xyclub.practice.server.entity.po.PracticeDetailPO;
import com.xyclub.practice.server.entity.po.PracticePO;
import com.xyclub.practice.server.entity.po.PracticeSetDetailPO;
import com.xyclub.practice.server.service.PracticeDetailService;
import com.xyclub.practice.server.util.DateUtils;
import com.xyclub.practice.server.util.LoginUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 练习详情服务实现：交卷时保存练习整体信息并补全未作答的题目记录
 */
@Service
@Slf4j
public class PracticeDetailServiceImpl implements PracticeDetailService {

    /**
     * 练习详情 DAO
     */
    @Resource
    private PracticeDetailDao practiceDetailDao;

    /**
     * 套题 DAO
     */
    @Resource
    private PracticeSetDao practiceSetDao;

    /**
     * 套题内容 DAO
     */
    @Resource
    private PracticeSetDetailDao practiceSetDetailDao;

    /**
     * 练习 DAO
     */
    @Resource
    private PracticeDao practiceDao;

    /**
     * 提交练题情况：保存/更新练习记录、计算正确率、热度+1、补全未答题目记录
     *
     * @param req 提交参数
     * @return 是否提交成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public Boolean submit(SubmitPracticeDetailReq req) {
        PracticePO practicePO = new PracticePO();
        Long practiceId = req.getPracticeId();
        Long setId = req.getSetId();
        practicePO.setSetId(setId);
        // 前端用时格式 HHmmss，转为 HH:mm:ss 存储
        String timeUse = req.getTimeUse();
        String hour = timeUse.substring(0, 2);
        String minute = timeUse.substring(2, 4);
        String second = timeUse.substring(4, 6);
        practicePO.setTimeUse(hour + ":" + minute + ":" + second);
        practicePO.setSubmitTime(DateUtils.parseStrToDate(req.getSubmitTime()));
        practicePO.setCompleteStatus(CompleteStatusEnum.COMPLETE.getCode());
        practicePO.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        practicePO.setCreatedBy(LoginUtil.getLoginId());
        practicePO.setCreatedTime(new Date());
        // 计算正确率 = 正确题数 / 套题总题数
        Integer correctCount = practiceDetailDao.selectCorrectCount(practiceId);
        List<PracticeSetDetailPO> practiceSetDetailPOS = practiceSetDetailDao.selectBySetId(setId);
        Integer totalCount = practiceSetDetailPOS.size();
        BigDecimal correctRate = new BigDecimal(correctCount).divide(new BigDecimal(totalCount), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100.00"));
        practicePO.setCorrectRate(correctRate);
        // 练习记录不存在则新增，存在则更新
        PracticePO po = practiceDao.selectById(practiceId);
        if (Objects.isNull(po)) {
            practiceDao.insert(practicePO);
        } else {
            practicePO.setId(practiceId);
            practiceDao.update(practicePO);
        }
        // 套题热度 +1
        practiceSetDao.updateHeat(setId);
        // 补全剩余题目的作答记录（未作答的题记为错误、空答案）
        List<PracticeDetailPO> practiceDetailPOList = practiceDetailDao.selectByPracticeId(practiceId);
        List<PracticeSetDetailPO> minusList = practiceSetDetailPOS.stream()
                .filter(item -> !practiceDetailPOList.stream()
                        .map(e -> e.getSubjectId())
                        .collect(Collectors.toList())
                        .contains(item.getSubjectId()))
                .collect(Collectors.toList());
        if (log.isInfoEnabled()) {
            log.info("题目差集{}", JSON.toJSONString(minusList));
        }
        if (CollectionUtils.isNotEmpty(minusList)) {
            minusList.forEach(e -> {
                PracticeDetailPO practiceDetailPO = new PracticeDetailPO();
                practiceDetailPO.setPracticeId(practiceId);
                practiceDetailPO.setSubjectType(e.getSubjectType());
                practiceDetailPO.setSubjectId(e.getSubjectId());
                practiceDetailPO.setAnswerStatus(0);
                practiceDetailPO.setAnswerContent("");
                practiceDetailPO.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
                practiceDetailPO.setCreatedTime(new Date());
                practiceDetailPO.setCreatedBy(LoginUtil.getLoginId());
                practiceDetailDao.insertSingle(practiceDetailPO);
            });
        }
        return true;
    }

}
