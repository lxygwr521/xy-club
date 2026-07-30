package com.xyclub.subject.infra.config;

import com.xyclub.subject.common.util.LoginUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Fills common audit fields such as createdBy and updateTime before MyBatis writes.
 */
@Component
@Slf4j
@Intercepts({@Signature(type = Executor.class, method = "update", args = {
        MappedStatement.class, Object.class
})})
public class MybatisInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        Object parameter = invocation.getArgs()[1];
        if (parameter == null) {
            return invocation.proceed();
        }

        String loginId = LoginUtil.getLoginId();
        if (StringUtils.isBlank(loginId)) {
            return invocation.proceed();
        }

        if (SqlCommandType.INSERT == sqlCommandType || SqlCommandType.UPDATE == sqlCommandType) {
            replaceEntityProperty(parameter, loginId, sqlCommandType);
        }
        return invocation.proceed();
    }

    private void replaceEntityProperty(Object parameter, String loginId, SqlCommandType sqlCommandType) {
        if (parameter instanceof Map) {
            replaceMap((Map<?, ?>) parameter, loginId, sqlCommandType);
        } else {
            replace(parameter, loginId, sqlCommandType);
        }
    }

    private void replaceMap(Map<?, ?> parameter, String loginId, SqlCommandType sqlCommandType) {
        for (Object val : parameter.values()) {
            replace(val, loginId, sqlCommandType);
        }
    }

    private void replace(Object parameter, String loginId, SqlCommandType sqlCommandType) {
        if (SqlCommandType.INSERT == sqlCommandType) {
            dealInsert(parameter, loginId);
        } else {
            dealUpdate(parameter, loginId);
        }
    }

    private void dealUpdate(Object parameter, String loginId) {
        Field[] fields = getAllFields(parameter);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(parameter);
                if (Objects.nonNull(value)) {
                    field.setAccessible(false);
                    continue;
                }
                if ("updateBy".equals(field.getName())) {
                    field.set(parameter, loginId);
                } else if ("updateTime".equals(field.getName())) {
                    field.set(parameter, new Date());
                }
                field.setAccessible(false);
            } catch (Exception e) {
                log.error("dealUpdate.error:{}", e.getMessage(), e);
            }
        }
    }

    private void dealInsert(Object parameter, String loginId) {
        Field[] fields = getAllFields(parameter);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(parameter);
                if (Objects.nonNull(value)) {
                    field.setAccessible(false);
                    continue;
                }
                if ("isDeleted".equals(field.getName())) {
                    field.set(parameter, 0);
                } else if ("createdBy".equals(field.getName())) {
                    field.set(parameter, loginId);
                } else if ("createdTime".equals(field.getName())) {
                    field.set(parameter, new Date());
                }
                field.setAccessible(false);
            } catch (Exception e) {
                log.error("dealInsert.error:{}", e.getMessage(), e);
            }
        }
    }

    private Field[] getAllFields(Object object) {
        Class<?> clazz = object.getClass();
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        Field[] fields = new Field[fieldList.size()];
        fieldList.toArray(fields);
        return fields;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
