package com.xyclub.practice.api.req;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 获取练习题列表请求参数
 */
@Data
public class GetPracticeSubjectListReq implements Serializable {

    /**
     * 分类与标签组合的 ids（格式：分类ID-标签ID，如 "2-1"）
     */
    private List<String> assembleIds;

}
