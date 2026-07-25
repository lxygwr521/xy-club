package com.xyclub.auth.api;

import com.xyclub.auth.entity.AuthUserDTO;
import com.xyclub.auth.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Feign contract exposed by auth service for user queries.
 */

//@FeignClient 本质是一个声明式 HTTP 客户端——把 Java 方法调用翻译成 HTTP请求，远程服务（xyclub-auth-dev）的 Controller 才是真正的"实现"
@FeignClient("xyclub-auth-dev")
public interface UserFeignService {

    @RequestMapping("/user/getUserInfo")
    Result<AuthUserDTO> getUserInfo(@RequestBody AuthUserDTO authUserDTO);

}
