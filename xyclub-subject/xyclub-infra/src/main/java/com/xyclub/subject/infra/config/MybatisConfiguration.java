package com.xyclub.subject.infra.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 全局配置。
 *
 * 当前主要用于注册 SQL 日志拦截器，方便在开发和排查问题时看到
 * Mapper 方法最终执行的 SQL 以及绑定后的参数值。
 *
 * @author lxy
 * @date 2026/07/15 14:14
 **/
@Configuration
public class MybatisConfiguration {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        // 注册 MyBatis Plus 内部拦截器，用于输出完整 SQL 日志。
        mybatisPlusInterceptor.addInnerInterceptor(new MybatisPlusAllSqlLog());
        return mybatisPlusInterceptor;
    }

}
