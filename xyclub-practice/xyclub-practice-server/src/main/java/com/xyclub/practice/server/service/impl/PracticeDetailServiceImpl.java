package com.xyclub.practice.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.xyclub.practice.api.enums.CompleteStatusEnum;
import com.xyclub.practice.api.enums.AnswerStatusEnum;
import com.xyclub.practice.api.enums.IsDeletedFlagEnum;
import com.xyclub.practice.api.enums.SubjectInfoTypeEnum;
import com.xyclub.practice.api.req.GetReportReq;
import com.xyclub.practice.api.req.GetScoreDetailReq;
import com.xyclub.practice.api.req.GetSubjectDetailReq;
import com.xyclub.practice.api.req.SubmitPracticeDetailReq;
import com.xyclub.practice.api.req.SubmitSubjectDetailReq;
import com.xyclub.practice.api.vo.PracticeSubjectOptionVO;
import com.xyclub.practice.api.vo.ReportSkillVO;
import com.xyclub.practice.api.vo.ReportVO;
import com.xyclub.practice.api.vo.ScoreDetailVO;
import com.xyclub.practice.api.vo.SubjectDetailVO;
import com.xyclub.practice.server.dao.PracticeDao;
import com.xyclub.practice.server.dao.PracticeDetailDao;
import com.xyclub.practice.server.dao.PracticeSetDao;
import com.xyclub.practice.server.dao.PracticeSetDetailDao;
import com.xyclub.practice.server.dao.SubjectDao;
import com.xyclub.practice.server.dao.SubjectJudgeDao;
import com.xyclub.practice.server.dao.SubjectLabelDao;
import com.xyclub.practice.server.dao.SubjectMappingDao;
import com.xyclub.practice.server.dao.SubjectMultipleDao;
import com.xyclub.practice.server.dao.SubjectRadioDao;
import com.xyclub.practice.server.entity.dto.SubjectDTO;
import com.xyclub.practice.server.entity.dto.SubjectDetailDTO;
import com.xyclub.practice.server.entity.dto.SubjectOptionDTO;
import com.xyclub.practice.server.entity.po.PracticeDetailPO;
import com.xyclub.practice.server.entity.po.PracticePO;
import com.xyclub.practice.server.entity.po.PracticeSetDetailPO;
import com.xyclub.practice.server.entity.po.PracticeSetPO;
import com.xyclub.practice.server.entity.po.SubjectJudgePO;
import com.xyclub.practice.server.entity.po.SubjectLabelPO;
import com.xyclub.practice.server.entity.po.SubjectMappingPO;
import com.xyclub.practice.server.entity.po.SubjectMultiplePO;
import com.xyclub.practice.server.entity.po.SubjectPO;
import com.xyclub.practice.server.entity.po.SubjectRadioPO;
import com.xyclub.practice.server.service.PracticeDetailService;
import com.xyclub.practice.server.util.DateUtils;
import com.xyclub.practice.server.util.LoginUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

    /** 题目基础信息 DAO，用于查询题干和题型 */
    @Resource
    private SubjectDao subjectDao;

    /** 单选题选项 DAO，用于获取选项及正确标识 */
    @Resource
    private SubjectRadioDao subjectRadioDao;

    /** 多选题选项 DAO，用于获取选项及正确标识 */
    @Resource
    private SubjectMultipleDao subjectMultipleDao;

    /** 判断题答案 DAO，用于获取正确/错误标准答案 */
    @Resource
    private SubjectJudgeDao subjectJudgeDao;

    /** 题目映射 DAO，用于查询题目关联的标签 id */
    @Resource
    private SubjectMappingDao subjectMappingDao;

    /** 题目标签 DAO，用于批量查询标签名称 */
    @Resource
    private SubjectLabelDao subjectLabelDao;

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

    /**
     * 提交单道题目并保存作答进度。
     *在切换题目或者交卷时触发
     * @param req 当前题目的答案、题型和练习累计用时
     * @return 保存成功返回 true
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submitSubject(SubmitSubjectDetailReq req) {
        String loginId = LoginUtil.getLoginId();
        if (StringUtils.isBlank(loginId)) {
            throw new IllegalStateException("未获取到当前登录用户");
        }

        // 每答完一道题都刷新练习累计用时，继续练习时可恢复到最新进度。
        PracticePO practicePO = new PracticePO();
        practicePO.setId(req.getPracticeId());
        practicePO.setTimeUse(formatTimeUse(req.getTimeUse()));
        practicePO.setSubmitTime(new Date());
        practiceDao.update(practicePO);

        // 用户答案和标准答案都规范为升序逗号串，例如多选 [3,1] 统一为 "1,3"。
        String answerContent = normalizeAnswerContents(req.getAnswerContents());
        SubjectDTO subjectDTO = new SubjectDTO();
        subjectDTO.setSubjectId(req.getSubjectId());
        subjectDTO.setSubjectType(req.getSubjectType());
        SubjectDetailDTO subjectDetail = getSubjectDetail(subjectDTO);
        String correctAnswer = buildCorrectAnswer(req.getSubjectType(), subjectDetail);

        PracticeDetailPO practiceDetailPO = new PracticeDetailPO();
        practiceDetailPO.setPracticeId(req.getPracticeId());
        practiceDetailPO.setSubjectId(req.getSubjectId());
        practiceDetailPO.setSubjectType(req.getSubjectType());
        practiceDetailPO.setAnswerContent(answerContent);
        practiceDetailPO.setAnswerStatus(Objects.equals(correctAnswer, answerContent) ? 1 : 0);
        practiceDetailPO.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        practiceDetailPO.setCreatedBy(loginId);
        practiceDetailPO.setCreatedTime(new Date());

        // 返回上一题重新作答时覆盖原记录，避免同一用户同一道题产生重复明细。
        PracticeDetailPO existDetail = practiceDetailDao.selectDetail(
                req.getPracticeId(), req.getSubjectId(), loginId);
        if (Objects.isNull(existDetail)) {
            practiceDetailDao.insertSingle(practiceDetailPO);
        } else {
            practiceDetailPO.setId(existDetail.getId());
            practiceDetailDao.update(practiceDetailPO);
        }
        return true;
    }

    /**
     * 查询一次练习的逐题得分情况。
     * 将作答明细转换为结果页需要的轻量 VO，不返回用户答案等无关字段。
     *
     * @param req 练习 id
     * @return 每道题的题目 id、题型和是否正确
     */
    @Override
    public List<ScoreDetailVO> getScoreDetail(GetScoreDetailReq req) {
        List<PracticeDetailPO> practiceDetailPOList =
                practiceDetailDao.selectByPracticeId(req.getPracticeId());
        if (CollectionUtils.isEmpty(practiceDetailPOList)) {
            return Collections.emptyList();
        }

        List<ScoreDetailVO> scoreDetailList = new LinkedList<>();
        practiceDetailPOList.forEach(practiceDetailPO -> {
            ScoreDetailVO scoreDetailVO = new ScoreDetailVO();
            scoreDetailVO.setSubjectId(practiceDetailPO.getSubjectId());
            scoreDetailVO.setSubjectType(practiceDetailPO.getSubjectType());
            scoreDetailVO.setIsCorrect(practiceDetailPO.getAnswerStatus());
            scoreDetailList.add(scoreDetailVO);
        });
        return scoreDetailList;
    }

    /**
     * 组装答案解析页面所需的单题详情。
     * 流程：按题型加载题干、选项和标准答案，再查询本次练习的用户答案，
     * 最后通过题目映射关系补充标签名称并转换为统一 VO。
     *
     * @param req 练习 id、题目 id 和题目类型
     * @return 当前题目的完整答案解析信息
     */
    @Override
    public SubjectDetailVO getSubjectDetail(GetSubjectDetailReq req) {
        Long subjectId = req.getSubjectId();
        Integer subjectType = req.getSubjectType();

        SubjectDTO subjectDTO = new SubjectDTO();
        subjectDTO.setSubjectId(subjectId);
        subjectDTO.setSubjectType(subjectType);
        SubjectDetailDTO subjectDetail = getSubjectDetail(subjectDTO);

        List<PracticeSubjectOptionVO> optionVOList = new LinkedList<>();
        List<Integer> correctAnswer = new LinkedList<>();
        List<SubjectOptionDTO> optionList = subjectDetail.getOptionList();
        if (CollectionUtils.isNotEmpty(optionList)) {
            optionList.forEach(option -> {
                PracticeSubjectOptionVO optionVO = new PracticeSubjectOptionVO();
                optionVO.setOptionType(option.getOptionType());
                optionVO.setOptionContent(option.getOptionContent());
                optionVO.setIsCorrect(option.getIsCorrect());
                optionVOList.add(optionVO);
                if (Objects.equals(option.getIsCorrect(), 1)) {
                    correctAnswer.add(option.getOptionType());
                }
            });
        }

        // 判断题没有独立选项表，在这里统一构造“正确/错误”两个展示选项。
        if (Objects.equals(subjectType, SubjectInfoTypeEnum.JUDGE.getCode())) {
            Integer isCorrect = subjectDetail.getIsCorrect();
            PracticeSubjectOptionVO correctOption = new PracticeSubjectOptionVO();
            correctOption.setOptionType(1);
            correctOption.setOptionContent("正确");
            correctOption.setIsCorrect(Objects.equals(isCorrect, 1) ? 1 : 0);
            PracticeSubjectOptionVO errorOption = new PracticeSubjectOptionVO();
            errorOption.setOptionType(2);
            errorOption.setOptionContent("错误");
            errorOption.setIsCorrect(Objects.equals(isCorrect, 0) ? 1 : 0);
            optionVOList.add(correctOption);
            optionVOList.add(errorOption);
            correctAnswer.add(isCorrect);
        }

        // 将数据库中的逗号分隔答案还原为前端使用的数字集合。
        List<Integer> respondAnswer = new LinkedList<>();
        PracticeDetailPO practiceDetailPO = practiceDetailDao.selectAnswer(req.getPracticeId(), subjectId);
        String answerContent = practiceDetailPO.getAnswerContent();
        if (StringUtils.isNotBlank(answerContent)) {
            String[] answerArray = answerContent.split(",");
            for (String answer : answerArray) {
                respondAnswer.add(Integer.valueOf(answer));
            }
        }

        List<SubjectMappingPO> subjectMappingPOList = subjectMappingDao.getLabelIdsBySubjectId(subjectId);
        List<Long> labelIdList = new LinkedList<>();
        subjectMappingPOList.forEach(subjectMappingPO -> labelIdList.add(subjectMappingPO.getLabelId()));
        List<String> labelNameList = subjectLabelDao.getLabelNameByIds(labelIdList);

        SubjectDetailVO subjectDetailVO = new SubjectDetailVO();
        subjectDetailVO.setOptionList(optionVOList);
        subjectDetailVO.setSubjectParse(subjectDetail.getSubjectParse());
        subjectDetailVO.setSubjectName(subjectDetail.getSubjectName());
        subjectDetailVO.setCorrectAnswer(correctAnswer);
        subjectDetailVO.setRespondAnswer(respondAnswer);
        subjectDetailVO.setLabelNames(labelNameList);
        return subjectDetailVO;
    }

    /**
     * 生成练习评估报告。
     * 流程：根据练习定位套题名称，统计总体答对数量，再分别汇总所有题目和正确题目的标签次数，
     * 通过“标签正确次数 / 标签总出现次数”计算各技能正确率。
     *
     * @param req 练习 id
     * @return 套题标题、总体正确数和各标签技能正确率
     */
    @Override
    public ReportVO getReport(GetReportReq req) {
        Long practiceId = req.getPracticeId();
        PracticePO practicePO = practiceDao.selectById(practiceId);
        Long setId = practicePO.getSetId();
        PracticeSetPO practiceSetPO = practiceSetDao.selectById(setId);

        ReportVO reportVO = new ReportVO();
        reportVO.setTitle(practiceSetPO.getSetName());

        List<PracticeDetailPO> practiceDetailPOList = practiceDetailDao.selectByPracticeId(practiceId);
        if (CollectionUtils.isEmpty(practiceDetailPOList)) {
            return null;
        }

        int totalCount = practiceDetailPOList.size();
        List<PracticeDetailPO> correctPracticeDetailList = practiceDetailPOList.stream()
                .filter(detail -> Objects.equals(detail.getAnswerStatus(), AnswerStatusEnum.CORRECT.getCode()))
                .collect(Collectors.toList());
        reportVO.setCorrectSubject(correctPracticeDetailList.size() + "/" + totalCount);

        Map<Long, Integer> totalLabelCountMap = getSubjectLabelMap(practiceDetailPOList);
        Map<Long, Integer> correctLabelCountMap = getSubjectLabelMap(correctPracticeDetailList);
        List<ReportSkillVO> reportSkillList = new LinkedList<>();
        totalLabelCountMap.forEach((labelId, labelTotalCount) -> {
            SubjectLabelPO labelPO = subjectLabelDao.queryById(labelId);
            int labelCorrectCount = correctLabelCountMap.getOrDefault(labelId, 0);

            BigDecimal correctRate = BigDecimal.ZERO;
            if (!Objects.equals(labelTotalCount, 0)) {
                correctRate = new BigDecimal(String.valueOf(labelCorrectCount))
                        .divide(new BigDecimal(String.valueOf(labelTotalCount)), 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal("100"));
            }

            ReportSkillVO skillVO = new ReportSkillVO();
            skillVO.setName(labelPO.getLabelName());
            skillVO.setStar(correctRate);
            reportSkillList.add(skillVO);
        });

        if (log.isInfoEnabled()) {
            log.info("获取到的正确率{}", JSON.toJSONString(reportSkillList));
        }
        reportVO.setSkill(reportSkillList);
        return reportVO;
    }

    /**
     * 按题目关联标签统计出现次数。
     * 流程：遍历作答明细，根据题目 id 查询关联标签，并累加每个标签对应的题目数量。
     *
     * @param practiceDetailPOList 待统计的作答明细
     * @return 标签 id 与出现次数的映射
     */
    private Map<Long, Integer> getSubjectLabelMap(List<PracticeDetailPO> practiceDetailPOList) {
        if (CollectionUtils.isEmpty(practiceDetailPOList)) {
            return Collections.emptyMap();
        }

        Map<Long, Integer> labelCountMap = new HashMap<>();
        practiceDetailPOList.forEach(detail -> {
            List<SubjectMappingPO> subjectMappingList =
                    subjectMappingDao.getLabelIdsBySubjectId(detail.getSubjectId());
            subjectMappingList.forEach(subjectMapping ->
                    labelCountMap.merge(subjectMapping.getLabelId(), 1, Integer::sum));
        });

        if (log.isInfoEnabled()) {
            log.info("获取到的题目对应的标签map{}", JSON.toJSONString(labelCountMap));
        }
        return labelCountMap;
    }

    /**
     * 按题型查询题目基础信息和标准答案。
     * 单选、多选题从选项表读取正确标识，判断题从 subject_judge 读取布尔答案。
     *
     * @param dto 题目 id 和题型
     * @return 用于判题的题目详情
     */
    public SubjectDetailDTO getSubjectDetail(SubjectDTO dto) {
        SubjectPO subjectPO = subjectDao.selectById(dto.getSubjectId());
        if (Objects.isNull(subjectPO)) {
            throw new IllegalArgumentException("题目不存在！");
        }
        if (!Objects.equals(dto.getSubjectType(), subjectPO.getSubjectType())) {
            throw new IllegalArgumentException("题目类型与题目数据不一致！");
        }

        SubjectDetailDTO subjectDetailDTO = new SubjectDetailDTO();
        subjectDetailDTO.setId(subjectPO.getId());
        subjectDetailDTO.setSubjectName(subjectPO.getSubjectName());
        subjectDetailDTO.setSubjectParse(subjectPO.getSubjectParse());

        if (Objects.equals(dto.getSubjectType(), SubjectInfoTypeEnum.RADIO.getCode())) {
            subjectDetailDTO.setOptionList(toRadioOptionList(
                    subjectRadioDao.selectBySubjectId(subjectPO.getId())));
        } else if (Objects.equals(dto.getSubjectType(), SubjectInfoTypeEnum.MULTIPLE.getCode())) {
            subjectDetailDTO.setOptionList(toMultipleOptionList(
                    subjectMultipleDao.selectBySubjectId(subjectPO.getId())));
        } else if (Objects.equals(dto.getSubjectType(), SubjectInfoTypeEnum.JUDGE.getCode())) {
            SubjectJudgePO judgeSubjectPO = subjectJudgeDao.selectBySubjectId(subjectPO.getId());
            if (Objects.isNull(judgeSubjectPO) || Objects.isNull(judgeSubjectPO.getIsCorrect())) {
                throw new IllegalStateException("判断题未配置标准答案！");
            }
            subjectDetailDTO.setIsCorrect(judgeSubjectPO.getIsCorrect());
        }
        return subjectDetailDTO;
    }

    /**
     * 将前端累计用时从 HHmmss 转换成 HH:mm:ss；兼容旧前端传入的 "0"。
     */
    private String formatTimeUse(String timeUse) {
        String normalizedTimeUse = "0".equals(timeUse) ? "000000" : timeUse;
        if (normalizedTimeUse.length() != 6 || !StringUtils.isNumeric(normalizedTimeUse)) {
            throw new IllegalArgumentException("用时格式必须为HHmmss！");
        }
        return normalizedTimeUse.substring(0, 2) + ":"
                + normalizedTimeUse.substring(2, 4) + ":"
                + normalizedTimeUse.substring(4, 6);
    }

    /**
     * 复制并排序用户选择，避免直接修改请求对象中的集合。
     */
    private String normalizeAnswerContents(List<Integer> answerContents) {
        if (CollectionUtils.isEmpty(answerContents)) {
            return "";
        }
        List<Integer> sortedAnswers = new ArrayList<>(answerContents);
        Collections.sort(sortedAnswers);
        return StringUtils.join(sortedAnswers, ",");
    }

    /**
     * 将不同题型的标准答案统一转换成可比较的字符串。
     */
    private String buildCorrectAnswer(Integer subjectType, SubjectDetailDTO subjectDetail) {
        if (Objects.equals(subjectType, SubjectInfoTypeEnum.JUDGE.getCode())) {
            return String.valueOf(subjectDetail.getIsCorrect());
        }
        if (CollectionUtils.isEmpty(subjectDetail.getOptionList())) {
            throw new IllegalStateException("题目未配置选项！");
        }
        String correctAnswer = subjectDetail.getOptionList().stream()
                .filter(option -> Objects.equals(option.getIsCorrect(), 1))
                .map(SubjectOptionDTO::getOptionType)
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        if (StringUtils.isBlank(correctAnswer)) {
            throw new IllegalStateException("题目未配置标准答案！");
        }
        return correctAnswer;
    }

    /**
     * 将单选题持久化对象转换为判题所需的统一选项对象。
     */
    private List<SubjectOptionDTO> toRadioOptionList(List<SubjectRadioPO> optionPOList) {
        List<SubjectOptionDTO> optionDTOList = new LinkedList<>();
        if (CollectionUtils.isEmpty(optionPOList)) {
            return optionDTOList;
        }
        optionPOList.forEach(optionPO -> {
            SubjectOptionDTO optionDTO = new SubjectOptionDTO();
            optionDTO.setOptionType(optionPO.getOptionType());
            optionDTO.setOptionContent(optionPO.getOptionContent());
            optionDTO.setIsCorrect(optionPO.getIsCorrect());
            optionDTOList.add(optionDTO);
        });
        return optionDTOList;
    }

    /**
     * 将多选题持久化对象转换为判题所需的统一选项对象。
     */
    private List<SubjectOptionDTO> toMultipleOptionList(List<SubjectMultiplePO> optionPOList) {
        List<SubjectOptionDTO> optionDTOList = new LinkedList<>();
        if (CollectionUtils.isEmpty(optionPOList)) {
            return optionDTOList;
        }
        optionPOList.forEach(optionPO -> {
            SubjectOptionDTO optionDTO = new SubjectOptionDTO();
            optionDTO.setOptionType(optionPO.getOptionType());
            optionDTO.setOptionContent(optionPO.getOptionContent());
            optionDTO.setIsCorrect(optionPO.getIsCorrect());
            optionDTOList.add(optionDTO);
        });
        return optionDTOList;
    }

}
