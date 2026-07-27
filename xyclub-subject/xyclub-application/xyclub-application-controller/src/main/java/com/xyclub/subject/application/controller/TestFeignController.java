package com.xyclub.subject.application.controller;

import com.xyclub.subject.infra.basic.service.SubjectEsService;
import com.xyclub.subject.infra.entity.UserInfo;
import com.xyclub.subject.infra.rpc.UserRpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Temporary endpoint used to verify subject-to-auth Feign calls.
 */
@RestController
@RequestMapping("/subject/category")
@Slf4j
public class TestFeignController {

    @Resource
    private UserRpc userRpc;

    @Resource
    private SubjectEsService subjectEsService;

    @GetMapping("testFeign")
    public void testFeign() {
        UserInfo userInfo = userRpc.getUserInfo("jichi");
        log.info("testFeign.userInfo:{}", userInfo);
    }

    @GetMapping("testCreateIndex")
    public void testCreateIndex() {
        subjectEsService.createIndex();
    }

    @GetMapping("addDocs")
    public void addDocs() {
        subjectEsService.addDoc();
    }

    @GetMapping("find")
    public void find() {
        subjectEsService.find();
    }

    @GetMapping("search")
    public void search() {
        subjectEsService.search();
    }
}
