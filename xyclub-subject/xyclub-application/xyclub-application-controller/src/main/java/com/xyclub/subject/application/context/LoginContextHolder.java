package com.xyclub.subject.application.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Stores login data for the current request thread.
 */
public class LoginContextHolder {

    private static final String LOGIN_ID = "loginId";
    private static final InheritableThreadLocal<Map<String, Object>> THREAD_LOCAL = new InheritableThreadLocal<>();

    public static void set(String key, Object value) {
        getThreadLocalMap().put(key, value);
    }

    public static Object get(String key) {
        return getThreadLocalMap().get(key);
    }

    public static String getLoginId() {
        return (String) get(LOGIN_ID);
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }

    private static Map<String, Object> getThreadLocalMap() {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (Objects.isNull(map)) {
            map = new HashMap<>();
            THREAD_LOCAL.set(map);
        }
        return map;
    }
}
