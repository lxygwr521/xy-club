package com.xyclub.subject.application.controller;

import com.xyclub.subject.infra.basic.entity.SubjectCategory;
import com.xyclub.subject.infra.basic.service.SubjectCategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 题目控制器
 *
 * @author lxy
 * @date 2026/07/10 19:52
 */
@RestController
@RequestMapping("/subject")
public class SubjectController {

    @Resource
    private SubjectCategoryService subjectCategoryService;

    /**
     * 健康检查
     */
    @GetMapping("/test")
    public String test() {
        SubjectCategory subjectCategory = subjectCategoryService.queryById(1L);
        return subjectCategory == null ? "not found" : subjectCategory.getCategoryName();
    }

    /**
     * 根据ID获取题目
     */
    @GetMapping("/getById")
    public String getById(@RequestParam Long id) {
        return "subject: " + id;
    }

    /**
     * 新增题目
     */
    @PostMapping("/add")
    public String add(@RequestBody String subject) {
        return "add success: " + subject;
    }
}
