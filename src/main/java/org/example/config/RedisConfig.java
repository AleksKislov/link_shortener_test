package org.example.config;

import redis.clients.jedis.JedisPool;

public class RedisConfig {
    private static JedisPool jedisPool;

    private static void initPool() {
        if (jedisPool == null) {
            jedisPool = new JedisPool("localhost", 6379);
        }
    }

    public static JedisPool getPool() {
        if (jedisPool == null) initPool();
        return jedisPool;
    }
}
