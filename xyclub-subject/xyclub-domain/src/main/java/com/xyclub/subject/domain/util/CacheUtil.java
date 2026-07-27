package com.xyclub.subject.domain.util;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Small local cache helper for short-lived domain query results.
 */
@Component
public class CacheUtil<K, V> {
//最多缓存 5000 个键值对，10秒后过期的基于 Google Guava 的本地缓存
    private final Cache<String, String> localCache = CacheBuilder.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build();

    public List<V> getResult(String cacheKey, Class<V> clazz, Function<String, List<V>> function) {
//        1.获取缓存，如果存在直接返回
        String content = localCache.getIfPresent(cacheKey);
        if (StringUtils.hasText(content)) {
            return JSON.parseArray(content, clazz);
        }
//       2.Function<String, List<V>> function 函数式接口参数，代表一个输入为 String，输出为 List<V> 的函数，用于在缓存未命中时执行数据查询逻辑
        List<V> resultList = function.apply(cacheKey);
        //3.查询后写入缓存
        if (!CollectionUtils.isEmpty(resultList)) {
            localCache.put(cacheKey, JSON.toJSONString(resultList));
            return resultList;
        }
        return new ArrayList<>();
    }

//    public Map<K, V> getMapResult(String cacheKey, Class<V> clazz, Function<String, Map<K, V>> function) {
//        return new HashMap<>();
//    }
}
