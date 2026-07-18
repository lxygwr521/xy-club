package com.xyclub.oss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.xyclub")
public class OssApplication {

    public static void main(String[] args) {
        SpringApplication.run(OssApplication.class);
    }

}
