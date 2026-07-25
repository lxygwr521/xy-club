package com.xyclub.subject.application.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Passes the current login id to downstream Feign calls.
 * 在 Subject 服务通过 OpenFeign 调用其他服务时，自动把当前请求中的用户上下文 loginId
 *   继续放到 Feign 请求头里。
 */
public class FeignRequestInterceptor implements RequestInterceptor {

    private static final String LOGIN_ID_HEADER = "loginId";

    @Override
    public void apply(RequestTemplate requestTemplate) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return;
        }

        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String loginId = request.getHeader(LOGIN_ID_HEADER);
        if (StringUtils.hasText(loginId)) {
            requestTemplate.header(LOGIN_ID_HEADER, loginId);
        }
    }
}
