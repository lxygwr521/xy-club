package com.xyclub.practice.server.util;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具类
 */
@Slf4j
public class DateUtils {

    /**
     * 字符串转时间（yyyy-MM-dd HH:mm:ss）
     *
     * @param timestamp 时间字符串
     * @return 转换失败返回 null
     */
    public static Date parseStrToDate(String timestamp) {
        try {
            SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sm.parse(timestamp);
        } catch (Exception e) {
            log.error("parseDate异常{}", timestamp, e.getMessage(), e);
            return null;
        }
    }

}
