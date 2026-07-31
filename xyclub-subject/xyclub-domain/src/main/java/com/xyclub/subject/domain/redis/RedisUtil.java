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
//    RedisTemplate 是 Spring Data Redis 提供的一个高度封装的模板类，用于在 Java 应用中轻松地操作 Redis 数据库。
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
     * opsForValue()是操作字符串（String）类型数据的接口
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

    /**
     * 获取 Hash 结构中所有字段值，并在读取后逐条删除该 Hash 中的所有字段。
     * 该操作通过 SCAN 命令遍历 Hash，避免一次性加载全部字段到内存，
     * 适合处理大 Key 场景下的数据迁移或过期清理任务。
     *
     * @param key Redis Hash 的键名
     * @return Map<Object, Object></Object,> 包含该 Hash 中所有字段和值的 Map 副本，键为字段名，值为字段值
     */
    public Map<Object, Object> getHashAndDelete(String key) {
        // 1. 创建一个普通的 HashMap 用于存放结果，确保返回的数据与 Redis 中的数据隔离
        Map<Object, Object> map = new HashMap<>();

        // 2. 获取一个游标（Cursor），用于迭代 Redis 中的 Hash 数据
        // opsForHash()：获取操作 Hash 的入口
        // scan(key, ScanOptions.NONE)：从指定的 key 开始扫描，ScanOptions.NONE 表示无特殊限制，会扫描全部字段
        //Map.Entry	Map 中的一个条目，包含一个 Key 和一个 Value
        Cursor<Map.Entry<Object, Object>> cursor =
                redisTemplate.opsForHash().scan(key, ScanOptions.NONE);

        // 3. 遍历 Hash 中的每一个字段-值对
        while (cursor.hasNext()) {
            // 获取当前游标指向的 Entry（包含 field 和 value）
            Map.Entry<Object, Object> entry = cursor.next();

            // 4. 存入结果 Map 中
            map.put(entry.getKey(), entry.getValue());

            // 5. 从 Redis 中删除当前这个字段（field）,保证redis和mysql中数据一致性。
            // 注意：这是逐条删除，每次只删除一个 field
            redisTemplate.opsForHash().delete(key, entry.getKey());
        }

        // 6. 返回已保存的 Map
        return map;
    }

}
