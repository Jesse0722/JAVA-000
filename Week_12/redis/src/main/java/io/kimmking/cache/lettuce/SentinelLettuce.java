package io.kimmking.cache.lettuce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Lijiajun
 * @date 2021/01/06 11:13 AM
 */
public class SentinelLettuce {

    public static void lettuceClient() {
        RedisClient redisClient = RedisClient.create("redis://192.168.50.244:6380/");
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        connection.sync().auth("123456");
        System.out.println(connection.toString());

        RedisCommands<String, String> syncCommands = connection.sync();
        syncCommands.set("lettuceClient", "Hello, Lettuce Redis");
        System.out.println(syncCommands.get("key"));
    }

    public static void lettuceSentinel() {
        RedisURI redisURI = RedisURI.Builder
                .sentinel("192.168.50.244", 26379)
                .withSentinelMasterId("mymaster")
                .withPassword("123456")
                .build();
        RedisClient client = RedisClient.create(redisURI);
        StatefulRedisConnection<String, String> connection = client.connect();
        System.out.println(connection.toString());
        RedisCommands<String, String> syncCommands = connection.sync();
        syncCommands.set("lettuceSentinel", "Hello, Lettuce Sentinel");
        System.out.println(syncCommands.get("key"));
    }

    public static void main(String[] args) {
        lettuceClient();
        lettuceSentinel();
    }
}

