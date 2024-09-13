package com.light.framework.cache.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.support.atomic.RedisAtomicInteger;

/**
 * spring redistemplate api指定为String类型初始化
 * 
 * @author luban
 */
public interface IRedisClient {
    /**************************** key相关命令 ****************************/
    /**
     * 
     * @param key
     * @return
     * @see <a href= "https://redis.io/commands/exists/">exists</a>
     */
    Boolean exists(String key);

    /**
     * 
     * @param keys
     * @return
     * @see <a href= "https://redis.io/commands/del/">del</a>
     */
    Long del(String... keys);

    /**
     * 
     * @param pattern
     * @return
     * @see <a href= "https://redis.io/commands/keys/">keys</a>
     */
    Set<String> keys(String pattern);

    /**
     * 
     * @param key
     * @param seconds
     * @return
     * @see <a href= "https://redis.io/commands/expire/">expire</a>
     */
    Boolean expire(String key, long seconds);

    /**
     * 
     * @param key
     * @param millis
     * @return
     * @see <a href= "https://redis.io/commands/pexpire/">pexpire</a>
     */
    Boolean pExpire(String key, long millis);

    /**
     * 
     * @param key
     * @param unixTime
     * @return
     * @see <a href= "https://redis.io/commands/expireat/">expireat</a>
     */
    Boolean expireAt(String key, long unixTime);

    /**
     * 
     * @param key
     * @param unixTimeInMillis
     * @return
     * @see <a href= "https://redis.io/commands/pexpireat/">pexpireat</a>
     */
    Boolean pExpireAt(String key, long unixTimeInMillis);

    /**
     * 
     * @param key
     * @return
     * @see <a href= "https://redis.io/commands/ttl/">ttl</a>
     */
    Long ttl(String key);

    /**
     * 
     * @param key
     * @param timeUnit
     * @return
     * @see <a href= "https://redis.io/commands/ttl/">ttl</a>
     */
    Long ttl(String key, TimeUnit timeUnit);

    /**
     * 
     * @param key
     * @return
     * @see <a href= "https://redis.io/commands/pttl/">pttl</a>
     */
    Long pTtl(String key);

    /**
     * 
     * @param key
     * @param timeUnit
     * @return
     * @see <a href= "https://redis.io/commands/pttl/">pttl</a>
     */
    Long pTtl(String key, TimeUnit timeUnit);

    /**************************** string相关命令 ****************************/
    /**
     * @param key
     * 
     * @return
     * @see <a href= "https://redis.io/commands/get/">get</a>
     */
    String get(String key);

    /**
     * @param key
     * 
     * @return
     * @see <a href= "https://redis.io/commands/getdel/">getdel</a>
     */
    String getDel(String key);

    /**
     * @param key
     * @param timeout
     * @param unit
     * 
     * @return
     * @see <a href= "https://redis.io/commands/getex/">getex</a>
     */
    String getEx(String key, long timeout, TimeUnit unit);

    /**
     * @param key
     * @param start
     * @param end
     * @return
     * @see <a href= "https://redis.io/commands/getrange/">getrange</a>
     */
    String getRange(String key, long start, long end);

    /**
     * @param key
     * @param value
     * 
     * @see <a href= "https://redis.io/commands/set/">set</a>
     */
    void set(String key, String value);

    /**
     * @param key
     * @param value
     * @param timeout
     * @param unit
     * 
     * @see #set(String, String)
     */
    void set(String key, String value, long timeout, TimeUnit unit);

    /**
     * @param key
     * @param value
     * 
     * @see <a href= "https://redis.io/commands/setnx/">setnx</a>
     */
    Boolean setNX(String key, String value);

    /**
     * @param key
     * @param value
     * @param timeout
     * @param unit
     * 
     * @see #setNX(String, String)
     */
    Boolean setNX(String key, String value, long timeout, TimeUnit unit);

    /**
     * @param keys
     * 
     * @return
     * @see <a href= "https://redis.io/commands/mget/">mget</a>
     */
    List<String> mGet(String... keys);

    /**
     * @param map
     * 
     * @see <a href= "https://redis.io/commands/mset/">mset</a>
     */
    void mSet(Map<String, String> map);

    /**
     * @param map
     * 
     * @see <a href= "https://redis.io/commands/msetnx/">msetnx</a>
     */
    Boolean mSetNX(Map<String, String> map);

    /**
     * @param key
     * @return
     * @see <a href= "https://redis.io/commands/incr/">incr</a>
     */
    Long incr(String key);

    /**
     * @see <a href= "https://redis.io/commands/incrby/">incrby</a>
     * @param key
     * @param delta
     * @return
     */
    Long incrBy(String key, long delta);

    /**
     * @see <a href= "https://redis.io/commands/incrby/">incrby</a>
     * @param key
     * @param delta
     * @return
     */
    Double incrBy(String key, double delta);

    /**
     * @param key
     * @return
     * @see <a href= "https://redis.io/commands/decr/">decr</a>
     */
    Long decr(String key);

    /**
     * @see <a href= "https://redis.io/commands/decrby/">decrby</a>
     * @param key
     * @param delta
     * @return
     */
    Long decrBy(String key, long delta);

    /**
     * @param key
     * @return
     * @see <a href= "https://redis.io/commands/strlen/">strlen</a>
     */
    Long strLen(String key);

    /**
     * @param key
     * @param offset
     * @param value
     * @return
     * @see <a href= "https://redis.io/commands/setbit/">setbit</a>
     */
    Boolean setBit(String key, long offset, boolean value);

    /**
     * @param key
     * @param offset
     * @return
     * @see <a href= "https://redis.io/commands/getbit/">getbit</a>
     */
    Boolean getBit(String key, long offset);

    /**************************** list相关命令 ****************************/
    /**
     * @param key
     * @param index
     * @return
     * @see <a href= "https://redis.io/commands/lindex/">lindex</a>
     */
    String lIndex(String key, long index);

    /**
     * @param key
     * @param value
     * @return
     * @see <a href= "https://redis.io/commands/lpos/">lpos</a>
     */
    Long lPos(String key, String value);

    /**
     * @param key
     * @param value
     * @return *
     * @see #lPos(String, String)
     */
    Long lPosRev(String key, String value);

    /**
     * @param key
     * 
     * @return
     * @see <a href= "https://redis.io/commands/lpop/">lpop</a>
     */
    String lPop(String key);

    /**
     * 
     * @param key
     * @param timeout
     * @param unit
     * 
     * @return
     * @see <a href= "https://redis.io/commands/blpop/">blpop</a>
     */
    String bLPop(String key, long timeout, TimeUnit unit);

    /**
     * @param key
     * 
     * @return
     * @see <a href= "https://redis.io/commands/lpush/">lpush</a>
     */
    Long lPush(String key, String value);

    /**
     * 
     * @param key
     * @param values
     * 
     * @return
     * @see <a href= "https://redis.io/commands/lpush/">lpush</a>
     */
    Long lPush(String key, String... values);

    /**
     * 
     * @param key
     * @param value
     * 
     * @return
     * @see <a href= "https://redis.io/commands/lpushx/">lpushx</a>
     */
    Long lPushX(String key, String value);

    /**
     * 
     * @param key
     * @param pivot
     * @param value
     * 
     * @return
     * @see <a href= "https://redis.io/commands/linsert/">linsert</a>
     */
    Long lLInsert(String key, String pivot, String value);

    /**
     * 
     * @param key
     * @return
     * @see <a href= "https://redis.io/commands/llen/">lLen</a>
     */
    Long lLen(String key);

    /**
     * 
     * @param key
     * @param start
     * @param end
     * 
     * @return
     * @see <a href= "https://redis.io/commands/lrange/">lrange</a>
     */
    List<String> lRange(String key, long start, long end);

    /**
     * 
     * @param key
     * @param count
     * @param value
     * 
     * @return
     * @see <a href= "https://redis.io/commands/lrem/">lrem</a>
     */
    Long lRem(String key, long count, String value);

    /**
     * 
     * @param key
     * 
     * @return
     * @see <a href= "https://redis.io/commands/rpop/">rpop</a>
     */
    String rPop(String key);

    /**
     * 
     * @param key
     * @param timeout
     * @param unit
     * 
     * @return
     * @see <a href= "https://redis.io/commands/brpop/">brpop</a>
     */
    String bRPop(String key, long timeout, TimeUnit unit);

    /**
     * 
     * @param key
     * @param value
     * 
     * @return
     * @see <a href= "https://redis.io/commands/rpush/">rpush</a>
     */
    Long rPush(String key, String value);

    /**
     * 
     * @param key
     * @param values
     * 
     * @return
     * @see <a href= "https://redis.io/commands/rpush/">rpush</a>
     */
    Long rPush(String key, String... values);

    /**
     * 
     * @param key
     * @param value
     * 
     * @return
     * @see <a href= "https://redis.io/commands/rpushx/">rpushx</a>
     */
    Long rPushX(String key, String value);

    /**
     * 
     * @param key
     * @param pivot
     * @param value
     * 
     * @return
     * @see <a href= "https://redis.io/commands/linsert/">linsert</a>
     */
    Long rLInsert(String key, String pivot, String value);

    /**
     * 
     * @param key
     * @param index
     * @param value
     * 
     * @see <a href= "https://redis.io/commands/lset/">lset</a>
     */
    void lSet(String key, long index, String value);

    /**
     * 
     * @param key
     * @param start
     * @param end
     * 
     * @see <a href= "https://redis.io/commands/ltrim/">ltrim</a>
     */
    void lTrim(String key, long start, long end);

    /**************************** Set相关命令 ****************************/
    Long sAdd(String key, String... values);

    /**
     * 
     * @param key
     * @param value
     * 
     * @return
     * @see <a href= "https://redis.io/commands/sismember/">sismember</a>
     */
    Boolean sIsMember(String key, String value);

    /**
     * 
     * @param key
     * 
     * @return
     * @see <a href= "https://redis.io/commands/smembers/">smembers</a>
     */
    Set<String> sMembers(String key);

    /**
     * 
     * @param key
     * @param value
     * @param destKey
     * 
     * @return
     * @see <a href= "https://redis.io/commands/smove/">smove</a>
     */
    Boolean sMove(String key, String value, String destKey);

    /**
     * 
     * @param key
     * @param values
     * 
     * @return
     * @see <a href= "https://redis.io/commands/srem/">srem</a>
     */
    Long sRem(String key, String... values);

    /**
     * 
     * @param key
     * 
     * @return
     * @see <a href= "https://redis.io/commands/spop/">spop</a>
     */
    String sPop(String key);

    /**
     * 
     * @param key
     * @param count
     * 
     * @return
     * @see <a href= "https://redis.io/commands/spop/">spop</a>
     */
    List<String> sPop(String key, long count);

    /**
     * 
     * @param key
     * @return
     * @see <a href= "https://redis.io/commands/scard/">scard</a>
     */
    Long sCard(String key);

    /**************************** ZSet相关命令 ****************************/
    Boolean zAdd(String key, String value, double score);

    /**
     * 
     * @param key
     * @param map
     * 
     * @return
     * @see <a href= "https://redis.io/commands/zadd/">zadd</a>
     */
    Long zAdd(String key, Map<String, Double> map);

    /**
     * 
     * @param key
     * @param value
     * @param delta
     * 
     * @return
     * @see <a href= "https://redis.io/commands/zincrBy/">zincrBy</a>
     */
    Double zIncrBy(String key, String value, double delta);

    /**
     * 
     * @param key
     * @param start
     * @param end
     * 
     * @return
     * @see <a href= "https://redis.io/commands/zrange/">zrange</a>
     */
    Set<String> zRange(String key, long start, long end);

    /**
     * 
     * @param key
     * @param start
     * @param end
     * 
     * @return
     * @see <a href= "https://redis.io/commands/zrevRange/">zrevRange</a>
     */
    Set<String> zRevRange(String key, long start, long end);

    /**
     * 
     * @param key
     * @param start
     * @param end
     * 
     * @return
     * @see <a href= "https://redis.io/commands/zrange/">zrange</a>
     */
    Map<String, Double> zRangeWithScores(String key, long start, long end);

    /**
     * 
     * @param key
     * @param start
     * @param end
     * 
     * @return
     * @see <a href= "https://redis.io/commands/zrange/">zrange</a>
     */
    Map<String, Double> zRevRangeWithScores(String key, long start, long end);

    /**
     * 
     * @param key
     * @param min
     * @param max
     * 
     * @return
     * @see <a href= "https://redis.io/commands/zrange/">zrange</a>
     */
    Set<String> zRangeByScore(String key, double min, double max);

    /**
     * 
     * @param key
     * @param min
     * @param max
     * @param offset
     * @param count
     * 
     * @return
     * @see <a href= "https://redis.io/commands/zrange/">zrange</a>
     */
    Set<String> zRangeByScore(String key, double min, double max, long offset, long count);

    /**
     * 
     * @param key
     * @param min
     * @param max
     * 
     * @return
     * @see <a href= "https://redis.io/commands/zrange/">zrange</a>
     */
    Set<String> reverseRangeByScore(String key, double min, double max);

    /**
     * 
     * @param key
     * @param min
     * @param max
     * @param offset
     * @param count
     * 
     * @return
     * @see <a href= "https://redis.io/commands/zrange/">zrange</a>
     */
    Set<String> reverseRangeByScore(String key, double min, double max, long offset, long count);

    /**
     * 
     * @param key
     * @param value
     * 
     * @return
     * @see <a href= "https://redis.io/commands/zrank/">zrank</a>
     */
    Long zRank(String key, String value);

    /**
     *
     * @param key
     * @param value
     * 
     * @return
     * @see <a href= "https://redis.io/commands/zrevrank/">zrevrank</a>
     */
    Long zRevRank(String key, String value);

    /**
     * 
     * @param key
     * @param values
     * 
     * @return
     * @see <a href= "https://redis.io/commands/zrem/">zrem</a>
     */
    Long zRem(String key, String... values);

    /**
     * 
     * @param key
     * @param start
     * @param end
     * 
     * @return
     * @see <a href= "https://redis.io/commands/zrem/">zrem</a>
     */
    Long zRemRange(String key, long start, long end);

    /**
     * 
     * @param key
     * @param min
     * @param max
     * 
     * @return
     * @see <a href= "https://redis.io/commands/zrem/">zrem</a>
     */
    Long zRemRangeByScore(String key, double min, double max);

    /**
     * 
     * @param key
     * @param value
     * 
     * @return
     * @see <a href= "https://redis.io/commands/zscore/">zscore</a>
     */
    Double zScore(String key, String value);

    /**
     * 
     * @param key
     * @param min
     * @param max
     * @return
     * @see <a href= "https://redis.io/commands/zcount/">zcount</a>
     */
    Long zCount(String key, double min, double max);

    /**
     * 
     * @param key
     * @return
     * @see <a href= "https://redis.io/commands/zcard/">zcard</a>
     */
    Long zCard(String key);

    /**************************** Hash相关命令 ****************************/
    /**
     * 
     * @param key
     * @param field
     * 
     * @return
     * @see <a href= "https://redis.io/commands/hget/">hget</a>
     */
    String hGet(String key, String field);

    /**
     * 
     * @param key
     * 
     * @return
     * @see <a href= "https://redis.io/commands/hget/">hgetall</a>
     */
    Map<String, String> hGetAll(String key);

    /**
     * 
     * @param key
     * @param field
     * @return
     * @see <a href= "https://redis.io/commands/hexists/">hexists</a>
     */
    Boolean hExists(String key, String field);

    /**
     * 
     * @param key
     * @param field
     * @param delta
     * @return
     * @see <a href= "https://redis.io/commands/hincrBy/">hincrBy</a>
     */
    Long hIncrBy(String key, String field, long delta);

    /**
     * 
     * @param key
     * @param field
     * @param delta
     * @return
     * @see <a href= "https://redis.io/commands/hincrBy/">hincrBy</a>
     */
    Double hIncrBy(String key, String field, double delta);

    /**
     * 
     * @param key
     * @return
     * @see <a href= "https://redis.io/commands/hkeys/">hkeys</a>
     */
    Set<String> hKeys(String key);

    /**
     * 
     * @param key
     * @return
     * @see <a href= "https://redis.io/commands/hlen/">hlen</a>
     */
    Long hLen(String key);

    /**
     * 
     * @param key
     * @param field
     * @return
     * @see <a href= "https://redis.io/commands/hstrLen/">hstrLen</a>
     */
    Long hStrLen(String key, String field);

    /**
     * 
     * @param key
     * @param field
     * @param value
     * 
     * @see <a href= "https://redis.io/commands/hset/">hset</a>
     */
    void hSet(String key, String field, String value);

    /**
     * 
     * @param key
     * @param field
     * @param value
     * 
     * @return
     * @see <a href= "https://redis.io/commands/hsetnx/">hsetnx</a>
     */
    Boolean hSetNX(String key, String field, String value);

    /**
     * 
     * @param key
     * @param map
     * 
     * @see <a href= "https://redis.io/commands/hmset/">hmset</a>
     */
    void hMSet(String key, Map<String, String> map);

    /**
     * 
     * @param key
     * @param fields
     * 
     * @return
     * @see <a href= "https://redis.io/commands/hmget/">hmget</a>
     */
    List<String> hMGet(String key, String... fields);

    /**
     * 
     * @param key
     * 
     * @return
     * @see <a href= "https://redis.io/commands/hvals/">hvals</a>
     */
    List<String> hVals(String key);

    /**
     * 
     * @param key
     * @param fields
     * @return
     * @see <a href= "https://redis.io/commands/hdel/">hdel</a>
     */
    Long hDel(String key, String... fields);

    /**************************** script相关命令 ****************************/

    /**************************** atomic相关命令 ****************************/
    RedisAtomicInteger getAtomicInteger(String key);
}
