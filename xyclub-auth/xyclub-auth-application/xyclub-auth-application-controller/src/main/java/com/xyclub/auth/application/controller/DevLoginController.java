package com.xyclub.auth.application.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.xyclub.auth.entity.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Development-only login endpoint for local API testing.
 */
@Profile("dev")
@RestController
@RequestMapping("/user/")
public class DevLoginController {

    @RequestMapping("devLogin")
    public Result<SaTokenInfo> devLogin(@RequestParam("loginId") String loginId) {
        if (StringUtils.isBlank(loginId)) {
            return Result.fail("loginId cannot be blank");
        }
        StpUtil.login(loginId);
        return Result.ok(StpUtil.getTokenInfo());
    }
}
