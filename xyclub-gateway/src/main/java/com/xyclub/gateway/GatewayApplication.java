package com.xyclub.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 网关启动器
 *   完整请求链路是：
 *
 *   前端
 *     |
 *     v
 *   xyclub-gateway
 *     |
 *     v
 *   SaTokenConfigure / SaReactorFilter 先鉴权
 *     |
 *     | 鉴权通过
 *     v
 *   Gateway 路由转发到 xyclub-auth / xyclub-subject / xyclub-oss
 *
 *   如果鉴权失败，请求不会继续转发到后端服务
 * @author: ChickenWing
 * @date: 2023/10/11
 */
@SpringBootApplication
@ComponentScan("com.xyclub")
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class);
    }
}
