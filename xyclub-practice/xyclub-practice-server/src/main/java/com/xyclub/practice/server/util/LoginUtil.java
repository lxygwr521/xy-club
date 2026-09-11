package com.xyclub.practice.server.util;

import com.xyclub.practice.server.config.context.LoginContextHolder;

/**
 * 用户登录工具类
 */
public class LoginUtil {

    /**
     * 获取当前登录用户 id
     *
     * @return 登录用户 id（来自请求头 loginId）
     */
    public static String getLoginId() {
        return LoginContextHolder.getLoginId();
    }


}
