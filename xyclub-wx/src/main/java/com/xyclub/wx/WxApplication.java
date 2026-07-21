package com.xyclub.wx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 微信服务启动器
 *
 * @author lxy
 * @date 2026-07-20
 */
@SpringBootApplication
@ComponentScan("com.xyclub")
public class WxApplication {

    public static void main(String[] args) {
        SpringApplication.run(WxApplication.class);
    }

}
