package com.xyclub.auth.domain.service;


import cn.dev33.satoken.stp.SaTokenInfo;
import com.xyclub.auth.domain.entity.AuthUserBO;

/**
 * 用户领域service
 *
 * @author lxy
 * @date 2026-07-20
 */
public interface AuthUserDomainService {

    /**
     * 注册
     */
    Boolean register(AuthUserBO authUserBO);

    /**
     * 更新用户信息
     */
    Boolean update(AuthUserBO authUserBO);

    /**
     * 删除用户信息
     */
    Boolean delete(AuthUserBO authUserBO);

    SaTokenInfo doLogin(String validCode);

}
