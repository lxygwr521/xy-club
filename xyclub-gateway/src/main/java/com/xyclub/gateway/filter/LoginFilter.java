package com.xyclub.gateway.filter;

import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Passes the authenticated user id from gateway to downstream services.
 */
@Component
@Slf4j
public class LoginFilter implements GlobalFilter {

    private static final String LOGIN_ID_HEADER = "loginId";
    private static final String LOGIN_URL_SUFFIX = "/user/doLogin";

    @Override
    @SneakyThrows
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest.Builder mutate = request.mutate();
        String url = request.getURI().getPath();
        log.info("LoginFilter.filter.url:{}", url);
        if (url.endsWith(LOGIN_URL_SUFFIX)) {
            return chain.filter(exchange);
        }

        String loginId;
        try {
            // Gateway is WebFlux; bind the current exchange before using synchronous Sa-Token APIs.
            SaReactorSyncHolder.setContext(exchange);
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
            loginId = String.valueOf(tokenInfo.getLoginId());
        } finally {
            SaReactorSyncHolder.clearContext();
        }
        if (StringUtils.isEmpty(loginId) || "null".equals(loginId)) {
            throw new Exception("未获取到用户信息");
        }
        mutate.header(LOGIN_ID_HEADER, loginId);
        return chain.filter(exchange.mutate().request(mutate.build()).build());
    }

}
