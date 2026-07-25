package com.xyclub.auth.application.interceptor;

import com.xyclub.auth.application.context.LoginContextHolder;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Restores login context from Feign or gateway request headers.
 */
public class LoginInterceptor implements HandlerInterceptor {

    private static final String LOGIN_ID_HEADER = "loginId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        LoginContextHolder.set(LOGIN_ID_HEADER, request.getHeader(LOGIN_ID_HEADER));
        String ID = request.getHeader(LOGIN_ID_HEADER);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) {
        LoginContextHolder.remove();
    }
}
