package com.xyclub.practice.server.service.impl;

import com.xyclub.practice.api.enums.SubjectInfoTypeEnum;
import com.xyclub.practice.api.vo.SpecialPracticeCategoryVO;
import com.xyclub.practice.api.vo.SpecialPracticeLabelVO;
import com.xyclub.practice.api.vo.SpecialPracticeVO;
import com.xyclub.practice.server.dao.SubjectCategoryDao;
import com.xyclub.practice.server.dao.SubjectMappingDao;
import com.xyclub.practice.server.entity.dto.CategoryDTO;
import com.xyclub.practice.server.entity.po.CategoryPO;
import com.xyclub.practice.server.entity.po.LabelCountPO;
import com.xyclub.practice.server.entity.po.PrimaryCategoryPO;
import com.xyclub.practice.server.service.PracticeSetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
