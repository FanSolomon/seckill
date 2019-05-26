package com.ytz.seckill.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RedisService {

    @Autowired
    JedisPool jedisPool;

//    private static JedisPool pool = null;
//    static {
//        JedisPoolConfig config = new JedisPoolConfig();
//        //设置最大连接数
//        config.setMaxTotal(1000);
//        //设置最大空闲数
//        config.setMaxIdle(500);
//        //设置最大等待时间
//        config.setMaxWaitMillis(180000);
//        //在borrow一个jedis实例时，是否需要验证，若为true，则所有jedis实例均是可用的
//        config.setTestOnBorrow(true);
//        pool = new  JedisPool(config, "localhost", 6379, 3000);
//    }

    private JedisPoolConfig  getJedisPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(1000);
        config.setMaxIdle(500);
        config.setMaxWaitMillis(180000);
        return config;
    }

    private JedisPool pool = new  JedisPool(getJedisPoolConfig(), "localhost");

    /**
     * 获取当个对象
     * */
    public <T> T get(KeyPrefix prefix, String key,  Class<T> clazz) {
        Jedis jedis = null;
        try {
//            jedis =  jedisPool.getResource();
            jedis =  pool.getResource();
            //生成真正的key
            String realKey  = prefix.getPrefix() + key;
            String  str = jedis.get(realKey);
            T t =  stringToBean(str, clazz);
            return t;
        }  finally {
            returnToPool(jedis);
        }
    }

    /**
     * 设置对象
     * */
    public <T> boolean set(KeyPrefix prefix, String key,  T value) {
        Jedis jedis = null;
        try {
//            jedis =  jedisPool.getResource();
            jedis =  pool.getResource();
            String str = beanToString(value);
            if(str == null || str.length() <= 0) {
                return false;
            }
            //生成真正的key
            String realKey  = prefix.getPrefix() + key;
            int seconds =  prefix.expireSeconds();
            if(seconds <= 0) {
                jedis.set(realKey, str);
            }else {
                jedis.setex(realKey, seconds, str);
            }
            return true;
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * 判断key是否存在
     * */
    public <T> boolean exists(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis =  pool.getResource();
            //生成真正的key
            String realKey  = prefix.getPrefix() + key;
            return  jedis.exists(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * 删除
     * */
    public boolean delete(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis =  pool.getResource();
            //生成真正的key
            String realKey  = prefix.getPrefix() + key;
            long ret =  jedis.del(realKey);
            return ret > 0;
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * 增加值
     * */
    public <T> Long incr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis =  pool.getResource();
            //生成真正的key
            String realKey  = prefix.getPrefix() + key;
            return  jedis.incr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * 减少值
     * */
    public <T> Long decr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis =  pool.getResource();
            //生成真正的key
            String realKey  = prefix.getPrefix() + key;
            return  jedis.decr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    public boolean delete(KeyPrefix prefix) {
        if(prefix == null) {
            return false;
        }
        List<String> keys = scanKeys(prefix.getPrefix());
        if(keys==null || keys.size() <= 0) {
            return true;
        }
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.del(keys.toArray(new String[0]));
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if(jedis != null) {
                jedis.close();
            }
        }
    }

    public List<String> scanKeys(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            List<String> keys = new ArrayList<String>();
            String cursor = "0";
            ScanParams sp = new ScanParams();
            sp.match("*"+key+"*");
            sp.count(100);
            do{
                ScanResult<String> ret = jedis.scan(cursor, sp);
                List<String> result = ret.getResult();
                if(result!=null && result.size() > 0){
                    keys.addAll(result);
                }
                //再处理cursor
                cursor = ret.getStringCursor();
            }while(!cursor.equals("0"));
            return keys;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public static <T> String beanToString(T value) {
        if(value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if(clazz == int.class || clazz == Integer.class) {
            return ""+value;
        }else if(clazz == String.class) {
            return (String)value;
        }else if(clazz == long.class || clazz == Long.class) {
            return ""+value;
        }else {
            return JSON.toJSONString(value);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T stringToBean(String str, Class<T> clazz) {
        if(str == null || str.length() <= 0 || clazz == null) {
            return null;
        }
        if(clazz == int.class || clazz == Integer.class) {
            return (T)Integer.valueOf(str);
        }else if(clazz == String.class) {
            return (T)str;
        }else if(clazz == long.class || clazz == Long.class) {
            return  (T)Long.valueOf(str);
        }else {
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }
    }

    private void returnToPool(Jedis jedis) {
        if(jedis != null) {
            jedis.close();
        }
    }
//
//    /**
//     * 加锁
//     * @param lockName       锁的key
//     * @param acquireTimeout 获取锁的超时时间，超过这个时间则放弃获取锁 毫秒
//     * @param timeout        锁的超时时间 毫秒
//     * @return 锁标识
//     */
//    public String lockWithTimeout(String lockName, long acquireTimeout, long timeout) {
//        Jedis conn = null;
//        String retIdentifier = null;
//        try {
//            // 获取连接
//            conn = pool.getResource();
//            // 随机生成一个value
//            String identifier = UUID.randomUUID().toString();
//            // 锁名，即key值
//            String lockKey = "lock:" + lockName;
//            // 超时时间，上锁后超过此时间则自动释放锁
//            int lockExpire = (int) (timeout / 1000);
//
//            // 获取锁的超时时间，超过这个时间则放弃获取锁
//            long end = System.currentTimeMillis() + acquireTimeout;
//            while (System.currentTimeMillis() < end) {
//                if (conn.setnx(lockKey, identifier) == 1) {
//                    conn.expire(lockKey, lockExpire);
//                    // 返回value值，用于释放锁时间确认
//                    retIdentifier = identifier;
//                    return retIdentifier;
//                }
//                // 返回-1代表key没有设置超时时间，为key设置一个超时时间
//                if (conn.ttl(lockKey) == -1) {
//                    conn.expire(lockKey, lockExpire);
//                }
//
//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (conn != null) {
//                conn.close();
//            }
//        }
//        return retIdentifier;
//    }
//
//    /**
//     * 释放锁
//     * @param lockName   锁的key
//     * @param identifier 释放锁的标识
//     * @return
//     */
//    public boolean releaseLock(String lockName, String identifier) {
//        Jedis conn = null;
//        String lockKey = "lock:" + lockName;
//        boolean retFlag = false;
//        try {
//            conn = pool.getResource();
//            while (true) {
//                // 监视lock，准备开始事务
//                conn.watch(lockKey);
//                // 通过前面返回的value值判断是不是该锁，若是该锁，则删除，释放锁
//                if (conn.get(lockKey)==null) {
//                    return true;
//                }
//                if (identifier.equals(conn.get(lockKey))) {
//                    Transaction transaction = conn.multi(); //返回一个事务控制对象
//                    transaction.del(lockKey);
//                    List<Object> results = transaction.exec();  //执行事务
//                    if (results == null) {
//                        continue;
//                    }
//                    retFlag = true;
//                }
//                conn.unwatch();
//                break;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (conn != null) {
//                conn.close();
//            }
//        }
//        return retFlag;
//    }
}
