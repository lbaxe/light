package com.light.framework.cache.redis;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;

/**
 * spring redis 工具类
 * 
 * @author ruoyi
 **/
public class RedisClientContext implements IRedisClient {
    private static final Logger logger = LoggerFactory.getLogger(RedisClientContext.class);

    private StringRedisTemplate stringRedisTemplate;

    private ValueOperations<String, String> valueOps;
    private ListOperations<String, String> listOps;
    private SetOperations<String, String> setOps;
    private ZSetOperations<String, String> zSetOps;
    private HashOperations<String, String, String> hashOps;

    public RedisClientContext(StringRedisTemplate StringRedisTemplate) {
        this.stringRedisTemplate = StringRedisTemplate;
        this.valueOps = stringRedisTemplate.opsForValue();
        this.listOps = stringRedisTemplate.opsForList();
        this.setOps = stringRedisTemplate.opsForSet();
        this.zSetOps = stringRedisTemplate.opsForZSet();
        this.hashOps = stringRedisTemplate.opsForHash();
    }

    byte[] rawString(String key) {
        return stringRedisTemplate.getStringSerializer().serialize(key);
    }

    @Override
    public Boolean exists(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    @Override
    public Long del(String... keys) {
        return stringRedisTemplate.delete(Arrays.asList(keys));
    }

    @Override
    public Set<String> keys(String pattern) {
        return stringRedisTemplate.keys(pattern);
    }

    @Override
    public Boolean expire(String key, long seconds) {
        return stringRedisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    @Override
    public Boolean pExpire(String key, long millis) {
        return stringRedisTemplate.expire(key, millis, TimeUnit.MILLISECONDS);
    }

    @Override
    public Boolean expireAt(String key, long unixTime) {
        return stringRedisTemplate.expireAt(key, Instant.ofEpochSecond(unixTime));
    }

    @Override
    public Boolean pExpireAt(String key, long unixTimeInMillis) {
        return stringRedisTemplate.expireAt(key, Instant.ofEpochMilli(unixTimeInMillis));
    }

    @Override
    public Long ttl(String key) {
        return stringRedisTemplate.execute((RedisCallback<Long>)connection -> connection.ttl(rawString(key)));
    }

    @Override
    public Long ttl(String key, TimeUnit timeUnit) {
        return stringRedisTemplate.execute((RedisCallback<Long>)connection -> connection.ttl(rawString(key), timeUnit));
    }

    @Override
    public Long pTtl(String key) {
        return stringRedisTemplate.execute((RedisCallback<Long>)connection -> connection.pTtl(rawString(key)));
    }

    @Override
    public Long pTtl(String key, TimeUnit timeUnit) {
        return stringRedisTemplate
            .execute((RedisCallback<Long>)connection -> connection.pTtl(rawString(key), timeUnit));
    }

    @Override
    public String get(String key) {
        return valueOps.get(key);
    }

    @Override
    public String getDel(String key) {
        return valueOps.getAndDelete(key);
    }

    @Override
    public String getEx(String key, long timeout, TimeUnit unit) {
        return valueOps.getAndExpire(key, timeout, unit);
    }

    @Override
    public String getRange(String key, long start, long end) {
        return valueOps.get(key, start, end);
    }

    @Override
    public void set(String key, String value) {
        valueOps.set(key, value);
    }

    @Override
    public void set(String key, String value, long timeout, TimeUnit unit) {
        valueOps.set(key, value, timeout, unit);
    }

    @Override
    public Boolean setNX(String key, String value) {
        return valueOps.setIfAbsent(key, value);
    }

    @Override
    public Boolean setNX(String key, String value, long timeout, TimeUnit unit) {
        return valueOps.setIfAbsent(key, value, timeout, unit);
    }

    @Override
    public List<String> mGet(String... keys) {
        return valueOps.multiGet(Arrays.asList(keys));
    }

    @Override
    public void mSet(Map<String, String> map) {
        valueOps.multiSet(map);
    }

    @Override
    public Boolean mSetNX(Map<String, String> map) {
        return valueOps.multiSetIfAbsent(map);
    }

    @Override
    public Long incr(String key) {
        return valueOps.increment(key);
    }

    @Override
    public Long incrBy(String key, long delta) {
        return valueOps.increment(key, delta);
    }

    @Override
    public Double incrBy(String key, double delta) {
        return valueOps.increment(key, delta);
    }

    @Override
    public Long decr(String key) {
        return valueOps.decrement(key);
    }

    @Override
    public Long decrBy(String key, long delta) {
        return valueOps.decrement(key, delta);
    }

    @Override
    public Long strLen(String key) {
        return valueOps.size(key);
    }

    @Override
    public Boolean setBit(String key, long offset, boolean value) {
        return valueOps.setBit(key, offset, value);
    }

    @Override
    public Boolean getBit(String key, long offset) {
        return valueOps.getBit(key, offset);
    }

    @Override
    public String lIndex(String key, long index) {
        return listOps.index(key, index);
    }

    @Override
    public Long lPos(String key, String value) {
        return listOps.indexOf(key, value);
    }

    @Override
    public Long lPosRev(String key, String value) {
        return listOps.lastIndexOf(key, value);
    }

    @Override
    public String lPop(String key) {
        return listOps.leftPop(key);
    }

    @Override
    public String bLPop(String key, long timeout, TimeUnit unit) {
        return listOps.leftPop(key, timeout, unit);
    }

    @Override
    public Long lPush(String key, String value) {
        return listOps.leftPush(key, value);
    }

    @Override
    public Long lPush(String key, String... values) {
        return listOps.leftPushAll(key, values);
    }

    @Override
    public Long lPushX(String key, String value) {
        return listOps.leftPushIfPresent(key, value);
    }

    @Override
    public Long lLInsert(String key, String pivot, String value) {
        return listOps.leftPush(key, pivot, value);
    }

    @Override
    public Long lLen(String key) {
        return listOps.size(key);
    }

    @Override
    public List<String> lRange(String key, long start, long end) {
        return listOps.range(key, start, end);
    }

    @Override
    public Long lRem(String key, long count, String value) {
        return listOps.remove(key, count, value);
    }

    @Override
    public String rPop(String key) {
        return listOps.rightPop(key);
    }

    @Override
    public String bRPop(String key, long timeout, TimeUnit unit) {
        return listOps.rightPop(key, timeout, unit);
    }

    @Override
    public Long rPush(String key, String value) {
        return listOps.rightPush(key, value);
    }

    @Override
    public Long rPush(String key, String... values) {
        return listOps.rightPushAll(key, values);
    }

    @Override
    public Long rPushX(String key, String value) {
        return listOps.rightPushIfPresent(key, value);
    }

    @Override
    public Long rLInsert(String key, String pivot, String value) {
        return listOps.rightPush(key, pivot, value);
    }

    @Override
    public void lSet(String key, long index, String value) {
        listOps.set(key, index, value);
    }

    @Override
    public void lTrim(String key, long start, long end) {
        listOps.trim(key, start, end);
    }

    @Override
    public Long sAdd(String key, String... values) {
        return setOps.add(key, values);
    }

    @Override
    public Boolean sIsMember(String key, String value) {
        return setOps.isMember(key, value);
    }

    @Override
    public Set<String> sMembers(String key) {
        return setOps.members(key);
    }

    @Override
    public Boolean sMove(String key, String value, String destKey) {
        return setOps.move(key, value, destKey);
    }

    @Override
    public Long sRem(String key, String... values) {
        return setOps.remove(key, values);
    }

    @Override
    public String sPop(String key) {
        return setOps.pop(key);
    }

    @Override
    public List<String> sPop(String key, long count) {
        return setOps.pop(key, count);
    }

    @Override
    public Long sCard(String key) {
        SetOperations setOps = stringRedisTemplate.opsForSet();
        return setOps.size(key);
    }

    @Override
    public Boolean zAdd(String key, String value, double score) {
        return zSetOps.add(key, value, score);
    }

    @Override
    public Long zAdd(String key, Map<String, Double> map) {
        Set<ZSetOperations.TypedTuple<String>> tuples = null;
        if (map != null) {
            tuples = new HashSet<>();
            for (Map.Entry<String, Double> entry : map.entrySet()) {
                tuples.add(new DefaultTypedTuple(entry.getKey(), entry.getValue()));
            }
        }
        return zSetOps.add(key, tuples);
    }

    @Override
    public Double zIncrBy(String key, String value, double delta) {
        return zSetOps.incrementScore(key, value, delta);
    }

    @Override
    public Set<String> zRange(String key, long start, long end) {
        return zSetOps.range(key, start, end);
    }

    @Override
    public Set<String> zRevRange(String key, long start, long end) {
        return zSetOps.reverseRange(key, start, end);
    }

    @Override
    public Map<String, Double> zRangeWithScores(String key, long start, long end) {
        Set<ZSetOperations.TypedTuple<String>> typedTupleSet = zSetOps.rangeWithScores(key, start, end);
        if (typedTupleSet == null) {
            return null;
        }
        return typedTupleSet.stream().collect(Collectors.toMap(e -> e.getValue(), v -> v.getScore(), (k1, k2) -> k2));
    }

    @Override
    public Map<String, Double> zRevRangeWithScores(String key, long start, long end) {
        Set<ZSetOperations.TypedTuple<String>> typedTupleSet = zSetOps.reverseRangeWithScores(key, start, end);
        if (typedTupleSet == null) {
            return null;
        }
        return typedTupleSet.stream().collect(Collectors.toMap(e -> e.getValue(), v -> v.getScore(), (k1, k2) -> k2));
    }

    @Override
    public Set<String> zRangeByScore(String key, double min, double max) {
        return zSetOps.rangeByScore(key, min, max);
    }

    @Override
    public Set<String> zRangeByScore(String key, double min, double max, long offset, long count) {
        return zSetOps.rangeByScore(key, min, max, offset, count);
    }

    @Override
    public Set<String> reverseRangeByScore(String key, double min, double max) {
        return zSetOps.reverseRangeByScore(key, min, max);
    }

    @Override
    public Set<String> reverseRangeByScore(String key, double min, double max, long offset, long count) {
        return zSetOps.reverseRangeByScore(key, min, max, offset, count);
    }

    @Override
    public Long zRank(String key, String value) {
        return zSetOps.rank(key, value);
    }

    @Override
    public Long zRevRank(String key, String value) {
        return zSetOps.reverseRank(key, value);
    }

    @Override
    public Long zRem(String key, String... values) {
        return zSetOps.remove(key, values);
    }

    @Override
    public Long zRemRange(String key, long start, long end) {
        return zSetOps.removeRange(key, start, end);
    }

    @Override
    public Long zRemRangeByScore(String key, double min, double max) {
        return zSetOps.removeRangeByScore(key, min, max);
    }

    @Override
    public Double zScore(String key, String value) {
        return zSetOps.score(key, value);
    }

    @Override
    public Long zCount(String key, double min, double max) {
        return zSetOps.count(key, min, max);
    }

    @Override
    public Long zCard(String key) {
        return zSetOps.size(key);
    }

    @Override
    public String hGet(String key, String field) {
        HashOperations<String, String, String> hashOps = stringRedisTemplate.opsForHash();
        return hashOps.get(key, field);
    }

    @Override
    public Map<String, String> hGetAll(String key) {
        return hashOps.entries(key);
    }

    @Override
    public Boolean hExists(String key, String field) {
        return hashOps.hasKey(key, field);
    }

    @Override
    public Long hIncrBy(String key, String field, long delta) {
        return hashOps.increment(key, field, delta);
    }

    @Override
    public Double hIncrBy(String key, String field, double delta) {
        return hashOps.increment(key, field, delta);
    }

    @Override
    public Set<String> hKeys(String key) {
        return hashOps.keys(key);
    }

    @Override
    public Long hLen(String key) {
        return hashOps.size(key);
    }

    @Override
    public Long hStrLen(String key, String field) {
        return hashOps.lengthOfValue(key, field);
    }

    @Override
    public void hSet(String key, String field, String value) {
        hashOps.put(key, field, value);
    }

    @Override
    public Boolean hSetNX(String key, String field, String value) {
        return hashOps.putIfAbsent(key, field, value);
    }

    @Override
    public void hMSet(String key, Map<String, String> map) {
        hashOps.putAll(key, map);
    }

    @Override
    public List<String> hMGet(String key, String... fields) {
        return hashOps.multiGet(key, Arrays.asList(fields));
    }

    @Override
    public List<String> hVals(String key) {
        return hashOps.values(key);
    }

    @Override
    public Long hDel(String key, String... fields) {
        return hashOps.delete(key, fields);
    }

    @Override
    public RedisAtomicInteger getAtomicInteger(String key) {
        return new RedisAtomicInteger(key, stringRedisTemplate.getConnectionFactory());
    }
}
