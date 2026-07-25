package com.xyclub.subject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.mybatis.spring.annotation.MapperScan;

/**
 *刷题微服务启动类
 */


@SpringBootApplication
@ComponentScan("com.xyclub")
@MapperScan("com.xyclub.subject.infra.basic.dao")
//启动时扫描并生成代理
@EnableFeignClients(basePackages = "com.xyclub")
public class SubjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(SubjectApplication.class, args);

    }
}
