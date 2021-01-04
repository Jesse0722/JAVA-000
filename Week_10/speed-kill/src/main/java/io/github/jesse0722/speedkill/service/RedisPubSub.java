package io.github.jesse0722.speedkill.service;

import io.github.jesse0722.speedkill.module.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author Lijiajun
 * @date 2021/01/04 10:54 AM
 */
@Service
@Slf4j
public class RedisPubSub {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 推送消息
     *
     * @param topic
     * @param object
     */
    public void publish(String topic, Object object) {
        redisTemplate.convertAndSend(topic, object);
    }

    @Component
    public static class MessageSubscriber {
        public void onMessage(Order order, String pattern) {
            log.info("topic {} received {} ", pattern, order);
        }

        @Bean
        public MessageListenerAdapter listener(Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer,
                                               MessageSubscriber subscriber) {
            MessageListenerAdapter adapter = new MessageListenerAdapter(subscriber, "onMessage");
            adapter.setSerializer(jackson2JsonRedisSerializer);
            adapter.afterPropertiesSet();
            return adapter;
        }

        @Bean
        public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                                       MessageListenerAdapter listener) {
            RedisMessageListenerContainer container = new RedisMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);
            container.addMessageListener(listener, new PatternTopic("/redis/order"));
            return container;
        }
    }
}
