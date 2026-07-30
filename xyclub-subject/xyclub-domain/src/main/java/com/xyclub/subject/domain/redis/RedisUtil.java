package com.xyclub.subject.domain.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Redis 常用操作工具类。
 */
@Component
@Slf4j
public class RedisUtil {

    @Resource
    private RedisTemplate redisTemplate;

    private static final String CACHE_KEY_SEPARATOR = ".";

    /**
     * 使用统一分隔符拼接缓存 key。
     */
    public String buildKey(String... strObjs) {
        return Stream.of(strObjs).collect(Collectors.joining(CACHE_KEY_SEPARATOR));
    }

    /**
     * 判断 key 是否存在。
     */
    public boolean exist(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 删除指定 key。
     */
    public boolean del(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 写入字符串缓存，不设置过期时间。
     */
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * key 不存在时写入字符串缓存，并设置过期时间。
     */
    public boolean setNx(String key, String value, Long time, TimeUnit timeUnit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, time, timeUnit);
    }

    /**
     * 读取字符串缓存。
     */
    public String get(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    /**
     * 向 ZSet 写入成员及其分值。
     */
    public Boolean zAdd(String key, String value, Long score) {
        return redisTemplate.opsForZSet().add(key, value, Double.valueOf(String.valueOf(score)));
    }

    /**
     * 统计 ZSet 成员数量。
     */
    public Long countZset(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * 按排名范围正序读取 ZSet 成员。
     */
    public Set<String> rangeZset(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 从 ZSet 删除指定成员。
     */
    public Long removeZset(String key, Object value) {
        return redisTemplate.opsForZSet().remove(key, value);
    }

    /**
     * 批量删除 ZSet 成员。
     */
    public void removeZsetList(String key, Set<String> value) {
        value.forEach(val -> redisTemplate.opsForZSet().remove(key, val));
    }

    /**
     * 查询 ZSet 成员分值。
     */
    public Double score(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 按分值范围读取 ZSet 成员。
     */
    public Set<String> rangeByScore(String key, long start, long end) {
        return redisTemplate.opsForZSet().rangeByScore(
                key, Double.valueOf(String.valueOf(start)), Double.valueOf(String.valueOf(end)));
    }

    /**
     * 累加 ZSet 成员分值，适合排行榜计数。
     */
    public Object addScore(String key, Object obj, double score) {
        return redisTemplate.opsForZSet().incrementScore(key, obj, score);
    }

    /**
     * 查询 ZSet 成员正序排名。
     */
    public Object rank(String key, Object obj) {
        return redisTemplate.opsForZSet().rank(key, obj);
    }

    /**
     * 按分值倒序读取成员和分值，用于排行榜展示。
     */
    public Set<ZSetOperations.TypedTuple<String>> rankWithScore(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }

    /**
     * 写入 Hash 字段，适合保存用户对题目的点赞状态。
     */
    public void putHash(String key, String hashKey, Object hashVal) {
        redisTemplate.opsForHash().put(key, hashKey, hashVal);
    }

    /**
     * 读取 Integer 类型缓存值。
     */
    public Integer getInt(String key) {
        return (Integer) redisTemplate.opsForValue().get(key);
    }

    /**
     * 对数值缓存做增减操作。
     */
    public void increment(String key, Integer count) {
        redisTemplate.opsForValue().increment(key, count);
    }

    public Map<Object, Object> getHashAndDelete(String key) {
        Map<Object, Object> map = new HashMap<>();
        Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(key, ScanOptions.NONE);
        while (cursor.hasNext()) {
            Map.Entry<Object, Object> entry = cursor.next();
            Object hashKey = entry.getKey();
            Object value = entry.getValue();
            map.put(hashKey, value);
            redisTemplate.opsForHash().delete(key, hashKey);
        }
        return map;
    }

}
