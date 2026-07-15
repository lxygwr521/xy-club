package com.xyclub.subject.infra.config;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * MyBatis 原生 SQL 执行耗时拦截器。
 *
 * 与 MybatisPlusAllSqlLog 关注“完整 SQL 内容”不同，
 * 该拦截器关注 SQL 执行耗时，并对超过 1s、5s、10s 的 SQL 打印分级日志。
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class,
                Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class,
                Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})})
public class SqlStatementInterceptor implements Interceptor {

    public static final Logger log = LoggerFactory.getLogger("sys-sql");

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            // 放行实际的 MyBatis 查询或更新逻辑。
            return invocation.proceed();
        } finally {
            // finally 确保即使 SQL 执行异常，也能记录已经消耗的时间。
            long timeConsuming = System.currentTimeMillis() - startTime;
            log.info("执行SQL:{}ms", timeConsuming);
            if (timeConsuming > 999 && timeConsuming < 5000) {
                log.info("执行SQL大于1s:{}ms", timeConsuming);
            } else if (timeConsuming >= 5000 && timeConsuming < 10000) {
                log.info("执行SQL大于5s:{}ms", timeConsuming);
            } else if (timeConsuming >= 10000) {
                log.info("执行SQL大于10s:{}ms", timeConsuming);
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        // 将当前拦截器织入 MyBatis Executor 对象。
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 当前拦截器暂无外部配置项，保留 MyBatis 插件接口默认入口。
    }
}
