package com.xyclub.subject.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.xyclub.subject.common.enums.IsDeletedFlagEnum;
import com.xyclub.subject.domain.convert.SubjectCategoryConverter;
import com.xyclub.subject.domain.entity.SubjectCategoryBO;
import com.xyclub.subject.domain.entity.SubjectLabelBO;
import com.xyclub.subject.domain.service.SubjectCategoryDomainService;
import com.xyclub.subject.domain.util.CacheUtil;
import com.xyclub.subject.infra.basic.entity.SubjectCategory;
import com.xyclub.subject.infra.basic.entity.SubjectLabel;
import com.xyclub.subject.infra.basic.entity.SubjectMapping;
import com.xyclub.subject.infra.basic.service.SubjectCategoryService;
import com.xyclub.subject.infra.basic.service.SubjectLabelService;
import com.xyclub.subject.infra.basic.service.SubjectMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SubjectCategoryDomainServiceImpl implements SubjectCategoryDomainService {

    @Resource
    private SubjectCategoryService subjectCategoryService;

    @Resource
    private SubjectMappingService subjectMappingService;

    @Resource
    private SubjectLabelService subjectLabelService;

    @Resource
    private ThreadPoolExecutor labelThreadPool;

    @Resource
    private CacheUtil<String, SubjectCategoryBO> cacheUtil;

    @Override
    public void add(SubjectCategoryBO subjectCategoryBO) {
        if (log.isInfoEnabled()) {
            log.info("SubjectCategoryController.add.bo:{}", JSON.toJSONString(subjectCategoryBO));
        }
        SubjectCategory subjectCategory = SubjectCategoryConverter.INSTANCE.convertBoToCategory(subjectCategoryBO);
        subjectCategory.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        subjectCategoryService.insert(subjectCategory);
    }

    @Override
    public List<SubjectCategoryBO> queryCategory(SubjectCategoryBO subjectCategoryBO) {
        if (log.isInfoEnabled()) {
            log.info("SubjectCategoryController.queryCategory.bo:{}", JSON.toJSONString(subjectCategoryBO));
        }
        SubjectCategory subjectCategory = SubjectCategoryConverter.INSTANCE.convertBoToCategory(subjectCategoryBO);
        subjectCategory.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        List<SubjectCategory> categoryList = subjectCategoryService.queryCategory(subjectCategory);
        List<SubjectCategoryBO> categoryBOList = SubjectCategoryConverter.INSTANCE.convertBoToCategory(categoryList);
        categoryBOList.forEach(bo -> {
            Integer subjectCount = subjectCategoryService.querySubjectCount(bo.getId());
            bo.setCount(subjectCount);
        });
        return categoryBOList;
    }

    @Override
    public Boolean update(SubjectCategoryBO subjectCategoryBO) {
        SubjectCategory subjectCategory = SubjectCategoryConverter.INSTANCE.convertBoToCategory(subjectCategoryBO);
        int count = subjectCategoryService.update(subjectCategory);
        return count > 0;
    }

    @Override
    public Boolean delete(SubjectCategoryBO subjectCategoryBO) {
        SubjectCategory subjectCategory = SubjectCategoryConverter.INSTANCE.convertBoToCategory(subjectCategoryBO);
        subjectCategory.setIsDeleted(IsDeletedFlagEnum.DELETED.getCode());
        int count = subjectCategoryService.update(subjectCategory);
        return count > 0;
    }

    /**
     * 查询一级分类下的二级分类及标签，先读本地缓存，未命中时再执行聚合查询。
     * 为什么要缓存：因为需要先查二级分类，然后再遍历二级分类查对应的标签并组装结果，链路较重。
     */
    @Override
    public List<SubjectCategoryBO> queryCategoryAndLabel(SubjectCategoryBO subjectCategoryBO) {
        Long categoryId = subjectCategoryBO.getId();
        String cacheKey = "categoryAndLabel." + categoryId;
        //这里的key实际没用到，对应调用时传入的cacheKey
        return cacheUtil.getResult(cacheKey, SubjectCategoryBO.class, key -> getSubjectCategoryBOS(categoryId));
    }

    /**
     * 查询并组装分类标签聚合数据，供缓存未命中时加载。
     */
    private List<SubjectCategoryBO> getSubjectCategoryBOS(Long categoryId) {
        SubjectCategory subjectCategory = new SubjectCategory();
        subjectCategory.setParentId(categoryId);
        subjectCategory.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        List<SubjectCategory> subjectCategoryList = subjectCategoryService.queryCategory(subjectCategory);
        if (log.isInfoEnabled()) {
            log.info("SubjectCategoryController.queryCategoryAndLabel.subjectCategoryList:{}",
                    JSON.toJSONString(subjectCategoryList));
        }

        List<SubjectCategoryBO> categoryBOList = SubjectCategoryConverter.INSTANCE.convertBoToCategory(subjectCategoryList);
        Map<Long, List<SubjectLabelBO>> labelMap = new HashMap<>();
        List<CompletableFuture<Map<Long, List<SubjectLabelBO>>>> futureList = categoryBOList.stream()
                .map(category -> CompletableFuture.supplyAsync(() -> getLabelBOList(category), labelThreadPool))
                .collect(Collectors.toList());

        // 汇总各异步任务结果，单个分类查询失败不影响其它分类回填。
        futureList.forEach(future -> {
            try {
                Map<Long, List<SubjectLabelBO>> resultMap = future.get();
                if (!CollectionUtils.isEmpty(resultMap)) {
                    labelMap.putAll(resultMap);
                }
            } catch (Exception e) {
                log.error("query category label failed", e);
            }
        });
        categoryBOList.forEach(categoryBO -> categoryBO.setLabelBOList(labelMap.get(categoryBO.getId())));
        return categoryBOList;
    }

    /**
     * 查询单个分类关联的标签，并按分类 ID 归集返回。
     */
    private Map<Long, List<SubjectLabelBO>> getLabelBOList(SubjectCategoryBO category) {
        if (log.isInfoEnabled()) {
            log.info("getLabelBOList:{}", JSON.toJSONString(category));
        }
        SubjectMapping subjectMapping = new SubjectMapping();
        subjectMapping.setCategoryId(category.getId());
        List<SubjectMapping> mappingList = subjectMappingService.queryLabelId(subjectMapping);
        if (CollectionUtils.isEmpty(mappingList)) {
            return null;
        }

        List<Long> labelIdList = mappingList.stream()
                .map(SubjectMapping::getLabelId)
                .collect(Collectors.toList());
        List<SubjectLabel> labelList = subjectLabelService.batchQueryById(labelIdList);
        List<SubjectLabelBO> labelBOList = new LinkedList<>();
        labelList.forEach(label -> {
            SubjectLabelBO subjectLabelBO = new SubjectLabelBO();
            subjectLabelBO.setId(label.getId());
            subjectLabelBO.setLabelName(label.getLabelName());
            subjectLabelBO.setCategoryId(label.getCategoryId());
            subjectLabelBO.setSortNum(label.getSortNum());
            labelBOList.add(subjectLabelBO);
        });

        Map<Long, List<SubjectLabelBO>> labelMap = new HashMap<>();
        labelMap.put(category.getId(), labelBOList);
        return labelMap;
    }
}
