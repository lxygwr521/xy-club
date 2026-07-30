package com.xyclub.subject.common.util;

import com.xyclub.subject.common.context.LoginContextHolder;

/**
 * Reads login data from request context.
 */
public class LoginUtil {

    public static String getLoginId() {
        return LoginContextHolder.getLoginId();
    }
}
