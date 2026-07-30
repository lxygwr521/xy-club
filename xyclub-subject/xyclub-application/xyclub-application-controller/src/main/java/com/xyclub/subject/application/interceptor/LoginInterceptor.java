package com.xyclub.subject.application.interceptor;

import com.xyclub.subject.common.context.LoginContextHolder;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Restores login context from the header passed by gateway.
 */
public class LoginInterceptor implements HandlerInterceptor {

    private static final String LOGIN_ID_HEADER = "loginId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String loginId = request.getHeader(LOGIN_ID_HEADER);
        if (StringUtils.hasText(loginId)) {
            LoginContextHolder.set(LOGIN_ID_HEADER, loginId);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) {
        LoginContextHolder.remove();
    }
}
