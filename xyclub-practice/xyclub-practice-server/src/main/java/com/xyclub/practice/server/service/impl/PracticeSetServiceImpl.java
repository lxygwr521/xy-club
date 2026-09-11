package com.xyclub.practice.server.service.impl;

import com.xyclub.practice.api.enums.CompleteStatusEnum;
import com.xyclub.practice.api.enums.IsDeletedFlagEnum;
import com.xyclub.practice.api.enums.SubjectInfoTypeEnum;
import com.xyclub.practice.api.req.GetPracticeSubjectsReq;
import com.xyclub.practice.api.vo.PracticeSetVO;
import com.xyclub.practice.api.vo.PracticeSubjectListVO;
import com.xyclub.practice.api.vo.PracticeSubjectDetailVO;
import com.xyclub.practice.api.vo.SpecialPracticeCategoryVO;
import com.xyclub.practice.api.vo.SpecialPracticeLabelVO;
import com.xyclub.practice.api.vo.SpecialPracticeVO;
import com.xyclub.practice.server.dao.PracticeDao;
import com.xyclub.practice.server.dao.PracticeDetailDao;
import com.xyclub.practice.server.dao.PracticeSetDao;
import com.xyclub.practice.server.dao.PracticeSetDetailDao;
import com.xyclub.practice.server.dao.SubjectCategoryDao;
import com.xyclub.practice.server.dao.SubjectDao;
import com.xyclub.practice.server.dao.SubjectLabelDao;
import com.xyclub.practice.server.dao.SubjectMappingDao;
import com.xyclub.practice.server.entity.dto.CategoryDTO;
import com.xyclub.practice.server.entity.dto.PracticeSubjectDTO;
import com.xyclub.practice.server.entity.po.CategoryPO;
import com.xyclub.practice.server.entity.po.LabelCountPO;
import com.xyclub.practice.server.entity.po.PracticeDetailPO;
import com.xyclub.practice.server.entity.po.PracticePO;
import com.xyclub.practice.server.entity.po.PracticeSetDetailPO;
import com.xyclub.practice.server.entity.po.PracticeSetPO;
import com.xyclub.practice.server.entity.po.PrimaryCategoryPO;
import com.xyclub.practice.server.entity.po.SubjectLabelPO;
import com.xyclub.practice.server.entity.po.SubjectPO;
import com.xyclub.practice.server.service.PracticeSetService;
import com.xyclub.practice.server.util.LoginUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 专项练习内容服务实现：
 * 按“一级分类 -> 二级分类 -> 标签”三级结构组装专项练习内容。
 */
@Service
@Slf4j
public class PracticeSetServiceImpl implements PracticeSetService {

    /**
     * 题目分类 DAO
     */
    @Resource
    private SubjectCategoryDao subjectCategoryDao;

    /**
     * 题目分类关系 DAO
     */
    @Resource
    private SubjectMappingDao subjectMappingDao;

    /**
     * 题目标签 DAO
     */
    @Resource
    private SubjectLabelDao subjectLabelDao;

    /**
     * 练习套题 DAO
     */
    @Resource
    private PracticeSetDao practiceSetDao;

    /**
     * 套题内容 DAO
     */
    @Resource
    private PracticeSetDetailDao practiceSetDetailDao;

    /**
     * 练习题目查询 DAO
     */
    @Resource
    private SubjectDao subjectDao;

    /**
     * 练习详情 DAO
     */
    @Resource
    private PracticeDetailDao practiceDetailDao;

    /**
     * 练习 DAO
     */
    @Resource
    private PracticeDao practiceDao;

    /**
     * 获取专项练习内容
     *
     * @return 一级分类列表，每个一级分类下包含二级分类及对应标签
     */
    @Override
    public List<SpecialPracticeVO> getSpecialPracticeContent() {
        List<SpecialPracticeVO> specialPracticeVOList = new LinkedList<>();
        // 专项练习仅展示 单选、多选、判断 三种题型
        List<Integer> subjectTypeList = new LinkedList<>();
        subjectTypeList.add(SubjectInfoTypeEnum.RADIO.getCode());
        subjectTypeList.add(SubjectInfoTypeEnum.MULTIPLE.getCode());
        subjectTypeList.add(SubjectInfoTypeEnum.JUDGE.getCode());
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setSubjectTypeList(subjectTypeList);
        // 查询包含题目的所有一级分类
        List<PrimaryCategoryPO> poList = subjectCategoryDao.getPrimaryCategory(categoryDTO);
        if (CollectionUtils.isEmpty(poList)) {
            return specialPracticeVOList;
        }
        poList.forEach(primaryCategoryPO -> {
            // 组装一级分类信息
            SpecialPracticeVO specialPracticeVO = new SpecialPracticeVO();
            specialPracticeVO.setPrimaryCategoryId(primaryCategoryPO.getParentId());
            CategoryPO categoryPO = subjectCategoryDao.selectById(primaryCategoryPO.getParentId());
            specialPracticeVO.setPrimaryCategoryName(categoryPO.getCategoryName());

            // 查询一级分类下的二级分类
            CategoryDTO categoryDTOTemp = new CategoryDTO();
            categoryDTOTemp.setCategoryType(2);
            categoryDTOTemp.setParentId(primaryCategoryPO.getParentId());
            List<CategoryPO> smallPoList = subjectCategoryDao.selectList(categoryDTOTemp);
            if (CollectionUtils.isEmpty(smallPoList)) {
                return;
            }
            List<SpecialPracticeCategoryVO> categoryList = new LinkedList();
            smallPoList.forEach(smallPo -> {
                // 查询二级分类下的标签列表
                List<SpecialPracticeLabelVO> labelVOList = getLabelVOList(smallPo.getId(), subjectTypeList);
                if (CollectionUtils.isEmpty(labelVOList)) {
                    return;
                }
                SpecialPracticeCategoryVO specialPracticeCategoryVO = new SpecialPracticeCategoryVO();
                specialPracticeCategoryVO.setCategoryId(smallPo.getId());
                specialPracticeCategoryVO.setCategoryName(smallPo.getCategoryName());
                // 拷贝标签信息到返回 VO
                List<SpecialPracticeLabelVO> labelList = new LinkedList<>();
                labelVOList.forEach(labelVo -> {
                    SpecialPracticeLabelVO specialPracticeLabelVO = new SpecialPracticeLabelVO();
                    specialPracticeLabelVO.setId(labelVo.getId());
                    specialPracticeLabelVO.setAssembleId(labelVo.getAssembleId());
                    specialPracticeLabelVO.setLabelName(labelVo.getLabelName());
                    labelList.add(specialPracticeLabelVO);
                });
                specialPracticeCategoryVO.setLabelList(labelList);
                categoryList.add(specialPracticeCategoryVO);
            });
            specialPracticeVO.setCategoryList(categoryList);
            specialPracticeVOList.add(specialPracticeVO);
        });
//        最终返回的 specialPracticeVOList 结构如下：
//        [
//        {
//            "primaryCategoryId": 1,
//                "primaryCategoryName": "编程技术",
//                "categoryList": [
//            {
//                "categoryId": 2,
//                    "categoryName": "Java",
//                    "labelList": [
//                { "id": 10, "assembleId": "2_10", "labelName": "基础语法" },
//                { "id": 20, "assembleId": "2_20", "labelName": "集合框架" }
//                     ]
//            },
//            {
//                "categoryId": 3,
//                    "categoryName": "Python",
//                    "labelList": [...]
//            }
//
//        }
//        ]
        return specialPracticeVOList;

    }

    /**
     * 开始练习：按单选/多选/判断题型随机抽题，生成实时套题并落库
     *
     * @param dto 练习题目查询条件
     * @return 生成的套题信息（含套题 id）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PracticeSetVO addPractice(PracticeSubjectDTO dto) {
        PracticeSetVO setVO = new PracticeSetVO();
        // 1. 按题型抽取题目
        List<PracticeSubjectDetailVO> practiceList = getPracticeList(dto);
        if (CollectionUtils.isEmpty(practiceList)) {
            return setVO;
        }
        // 2. 组装套题信息：套题类型为实时生成
        PracticeSetPO practiceSetPO = new PracticeSetPO();
        practiceSetPO.setSetType(1);
        List<String> assembleIds = dto.getAssembleIds();
        // 收集涉及的分类 id，用于生成套题名称
        Set<Long> categoryIdSet = new HashSet<>();
        assembleIds.forEach(assembleId -> {
            Long categoryId = Long.valueOf(assembleId.split("-")[0]);
            categoryIdSet.add(categoryId);
        });
        // 取前两个分类名生成套题名称，如 "缓存、数据库专项练习"
        StringBuffer setName = new StringBuffer();
        int i = 1;
        for (Long categoryId : categoryIdSet) {
            if (i > 2) {
                break;
            }
            CategoryPO categoryPO = subjectCategoryDao.selectById(categoryId);
            setName.append(categoryPO.getCategoryName());
            setName.append("、");
            i = i + 1;
        }
        setName.deleteCharAt(setName.length() - 1);
        if (i == 2) {
            setName.append("专项练习");
        } else {
            setName.append("等专项练习");
        }
        practiceSetPO.setSetName(setName.toString());
        // 取第一个标签归属的分类作为套题所属大类
        String labelId = assembleIds.get(0).split("-")[1];
        SubjectLabelPO labelPO = subjectLabelDao.queryById(Long.valueOf(labelId));
        practiceSetPO.setPrimaryCategoryId(labelPO.getCategoryId());
        practiceSetPO.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        practiceSetPO.setCreatedBy(LoginUtil.getLoginId());
        practiceSetPO.setCreatedTime(new Date());
        // 3. 保存套题，回填套题 id
        practiceSetDao.add(practiceSetPO);
        Long practiceSetId = practiceSetPO.getId();
        // 4. 保存套题与题目的关联明细
        practiceList.forEach(e -> {
            PracticeSetDetailPO detailPO = new PracticeSetDetailPO();
            detailPO.setSetId(practiceSetId);
            detailPO.setSubjectId(e.getSubjectId());
            detailPO.setSubjectType(e.getSubjectType());
            detailPO.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
            detailPO.setCreatedBy(LoginUtil.getLoginId());
            detailPO.setCreatedTime(new Date());
            practiceSetDetailDao.add(detailPO);
        });
        setVO.setSetId(practiceSetId);
        return setVO;
    }

    /**
     * 获取套卷题目信息：按 单选10 + 多选6 + 判断4 抽取，不足 20 题时用单选补齐
     *
     * @param dto 练习题目查询条件
     * @return 题目明细列表
     */
    private List<PracticeSubjectDetailVO> getPracticeList(PracticeSubjectDTO dto) {
        List<PracticeSubjectDetailVO> practiceSubjectListVOS = new LinkedList<>();
        // 已抽取题目 id 集合，避免重复抽题
        List<Long> excludeSubjectIds = new LinkedList<>();

        // 各题型题目数量，后续可优化到 nacos 动态配置
        Integer radioSubjectCount = 10;
        Integer multipleSubjectCount = 6;
        Integer judgeSubjectCount = 4;
        Integer totalSubjectCount = 20;
        // 查询单选
        dto.setSubjectCount(radioSubjectCount);
        dto.setSubjectType(SubjectInfoTypeEnum.RADIO.getCode());
        assembleList(dto, practiceSubjectListVOS, excludeSubjectIds);
        // 查询多选
        dto.setSubjectCount(multipleSubjectCount);
        dto.setSubjectType(SubjectInfoTypeEnum.MULTIPLE.getCode());
        assembleList(dto, practiceSubjectListVOS, excludeSubjectIds);
        // 查询判断
        dto.setSubjectCount(judgeSubjectCount);
        dto.setSubjectType(SubjectInfoTypeEnum.JUDGE.getCode());
        assembleList(dto, practiceSubjectListVOS, excludeSubjectIds);
        // 数量不足 20 时用单选补齐剩余题目
        if (practiceSubjectListVOS.size() == totalSubjectCount) {
            return practiceSubjectListVOS;
        }
        Integer remainCount = totalSubjectCount - practiceSubjectListVOS.size();
        dto.setSubjectCount(remainCount);
        dto.setSubjectType(1);
        assembleList(dto, practiceSubjectListVOS, excludeSubjectIds);
        return practiceSubjectListVOS;
    }

    /**
     * 按当前条件查询题目并追加到结果列表，同时记录已抽题目 id
     *
     * @param dto               查询条件
     * @param list              题目明细结果列表
     * @param excludeSubjectIds 已抽取题目 id 集合
     * @return 追加后的题目明细列表
     */
    private List<PracticeSubjectDetailVO> assembleList(PracticeSubjectDTO dto, List<PracticeSubjectDetailVO> list, List<Long> excludeSubjectIds) {
        dto.setExcludeSubjectIds(excludeSubjectIds);
        List<SubjectPO> subjectPOList = subjectDao.getPracticeSubject(dto);
        if (CollectionUtils.isEmpty(subjectPOList)) {
            return list;
        }
        subjectPOList.forEach(e -> {
            PracticeSubjectDetailVO vo = new PracticeSubjectDetailVO();
            vo.setSubjectId(e.getId());
            vo.setSubjectType(e.getSubjectType());
            excludeSubjectIds.add(e.getId());
            list.add(vo);
        });
        return list;
    }

    /**
     * 获取练习题目列表：根据套题 id 查询套题内容并组装标题
     *
     * @param req 请求参数（套题 id）
     * @return 练习标题与题目列表
     */
    @Override
    public PracticeSubjectListVO getSubjects(GetPracticeSubjectsReq req) {
        Long setId = req.getSetId();
        PracticeSubjectListVO vo = new PracticeSubjectListVO();
        List<PracticeSubjectDetailVO> practiceSubjectListVOS = new LinkedList<>();
        // 查询套题下的题目明细
        List<PracticeSetDetailPO> practiceSetDetailPOS = practiceSetDetailDao.selectBySetId(setId);
        if (CollectionUtils.isEmpty(practiceSetDetailPOS)) {
            return vo;
        }
        String loginId = LoginUtil.getLoginId();
        Long practiceId = req.getPracticeId();
        practiceSetDetailPOS.forEach(e -> {
            PracticeSubjectDetailVO practiceSubjectListVO = new PracticeSubjectDetailVO();
            practiceSubjectListVO.setSubjectId(e.getSubjectId());
            practiceSubjectListVO.setSubjectType(e.getSubjectType());
            // 继续练习时回填每道题的作答状态
            if (Objects.nonNull(practiceId)) {
                PracticeDetailPO practiceDetailPO = practiceDetailDao.selectDetail(practiceId, e.getSubjectId(), loginId);
                if (Objects.nonNull(practiceDetailPO) && StringUtils.isNotBlank(practiceDetailPO.getAnswerContent())) {
                    practiceSubjectListVO.setIsAnswer(1);
                } else {
                    practiceSubjectListVO.setIsAnswer(0);
                }
            }
            practiceSubjectListVOS.add(practiceSubjectListVO);
        });
        vo.setSubjectList(practiceSubjectListVOS);
        // 查询套题标题
        PracticeSetPO practiceSetPO = practiceSetDao.selectById(setId);
        vo.setTitle(practiceSetPO.getSetName());
        // 首次进入创建未完成练习记录；继续练习则刷新时间并恢复已用时
        if (Objects.isNull(practiceId)) {
            Long newPracticeId = insertUnCompletePractice(setId);
            vo.setPracticeId(newPracticeId);
        } else {
            updateUnCompletePractice(practiceId);
            PracticePO practicePO = practiceDao.selectById(practiceId);
            vo.setTimeUse(practicePO.getTimeUse());
            vo.setPracticeId(practiceId);
        }
        return vo;
    }

    /**
     * 创建一条未完成的练习记录（首次进入答题页时）
     *
     * @param practiceSetId 套题 id
     * @return 新练习记录 id
     */
    private Long insertUnCompletePractice(Long practiceSetId) {
        PracticePO practicePO = new PracticePO();
        practicePO.setSetId(practiceSetId);
        practicePO.setCompleteStatus(CompleteStatusEnum.NO_COMPLETE.getCode());
        practicePO.setTimeUse("00:00:00");
        practicePO.setSubmitTime(new Date());
        practicePO.setCorrectRate(new BigDecimal("0.00"));
        practicePO.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        practicePO.setCreatedBy(LoginUtil.getLoginId());
        practicePO.setCreatedTime(new Date());
        practiceDao.insert(practicePO);
        return practicePO.getId();
    }

    /**
     * 更新未完成练习记录（继续练习时刷新交卷时间）
     *
     * @param practiceId 练习 id
     */
    private void updateUnCompletePractice(Long practiceId) {
        PracticePO practicePO = new PracticePO();
        practicePO.setId(practiceId);
        practicePO.setSubmitTime(new Date());
        practiceDao.update(practicePO);
    }

    /**
     * 获取指定分类下各标签的题目数量，并组装为标签 VO 列表
     *
     * @param categoryId      二级分类ID
     * @param subjectTypeList 题目类型列表
     * @return 标签 VO 列表，无数据时返回空列表
     */
    private List<SpecialPracticeLabelVO> getLabelVOList(Long categoryId, List<Integer> subjectTypeList) {
        List<LabelCountPO> countPOList = subjectMappingDao.getLabelSubjectCount(categoryId, subjectTypeList);
        if (CollectionUtils.isEmpty(countPOList)) {
            return Collections.emptyList();
        }
        List<SpecialPracticeLabelVO> voList = new LinkedList<>();
        countPOList.forEach(countPo -> {
            SpecialPracticeLabelVO vo = new SpecialPracticeLabelVO();
            vo.setId(countPo.getLabelId());
            // 分类ID-标签ID 组合标识
            vo.setAssembleId(categoryId + "-" + countPo.getLabelId());
            // 标签名称由统计 SQL 直接查出，无需再查询标签表
            vo.setLabelName(countPo.getLabelName());
            voList.add(vo);
        });
        return voList;
    }

}
