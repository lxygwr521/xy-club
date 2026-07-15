package com.xyclub.subject.infra.config;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * MyBatis Plus SQL 日志拦截器。
 *
 * MyBatis 执行时 SQL 中的参数默认以 ? 占位符展示，不方便排查问题。
 * 该拦截器会在查询和更新执行前，将 BoundSql 中的占位符替换为真实参数，
 * 并把 Mapper 方法 ID 和完整 SQL 输出到 sys-sql 日志中。
 */
public class MybatisPlusAllSqlLog implements InnerInterceptor {

    public static final Logger log = LoggerFactory.getLogger("sys-sql");

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
                            ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        // 查询执行前打印 SQL，便于定位慢查询或参数错误。
        logInfo(boundSql, ms, parameter);
    }

    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) throws SQLException {
        // 更新、插入、删除执行前也统一打印 SQL。
        BoundSql boundSql = ms.getBoundSql(parameter);
        logInfo(boundSql, ms, parameter);
    }

    private static void logInfo(BoundSql boundSql, MappedStatement ms, Object parameter) {
        try {
            log.info("parameter = " + parameter);
            String sqlId = ms.getId();
            log.info("sqlId = " + sqlId);
            Configuration configuration = ms.getConfiguration();
            String sql = getSql(configuration, boundSql, sqlId);
            log.info("完整的sql:{}", sql);
        } catch (Exception e) {
            log.error("异常:{}", e.getLocalizedMessage(), e);
        }
    }

    public static String getSql(Configuration configuration, BoundSql boundSql, String sqlId) {
        // sqlId 是 Mapper XML 中具体语句的完整路径，配合 SQL 一起输出更容易定位来源。
        return sqlId + ":" + showSql(configuration, boundSql);
    }

    public static String showSql(Configuration configuration, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        // 合并换行和多余空格，让日志中的 SQL 保持单行，方便复制和排查。
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (!CollectionUtils.isEmpty(parameterMappings) && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                // 简单参数类型，例如 Long、String、Integer，直接替换第一个占位符。
                sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(parameterObject)));
            } else {
                // 复杂对象或 Map 参数，通过 MetaObject 按 ParameterMapping 中的属性名取值。
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        // foreach、动态 SQL 等场景的临时参数会放在 additionalParameter 中。
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                    } else {
                        // 参数缺失时保留提示，避免后续参数错位导致日志误判。
                        sql = sql.replaceFirst("\\?", "缺失");
                    }
                }
            }
        }
        return sql;
    }

    private static String getParameterValue(Object obj) {
        String value;
        if (obj instanceof String) {
            // 字符串参数加单引号，输出结果更接近可直接执行的 SQL。
            value = "'" + obj + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
                    DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(new Date()) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }
        }
        return value;
    }

}
