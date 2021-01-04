# 第十一周 分布式缓存

## 作业

### 基于 Redis 封装分布式数据操作

- 在 Java 中实现一个简单的分布式锁；
- 在 Java 中实现一个分布式计数器，模拟减库存。

分布式锁的实现思路：

利用redis单线程的特性，set nx ex 如果key不存在则设置一个key（lock）值，其value为随机值，返回true，如果key值已经存在说名已经 被其他线程设置过，则返回false。执行正常逻辑，执行完过后释放锁（del key），释放锁的时候需要判断value是否是当前线程所生成的。如果是则删除，如果不是，说明不是则说明释放了其他线程的锁，需要抛出异常。获取锁的时候用了setnx，对于redis其本身就是原子操作。但是释放锁的时候，需要先查询锁，比较其value与当前随机值是否相当，这个过程应该是原子操作，因此需要用lua脚本封装执行。

```java
 @Transactional
    public void updateStockWithRedisLock(String orderNo, long id, int count) {
        String redisLockKey = "product_" + id;
        String redisLockValue = UUID.randomUUID().toString();

        // set nx ex
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(redisLockKey, redisLockValue , 10, TimeUnit.SECONDS);
        if (flag) {
            SpeedKill speedKill = speedKillMapper.get(id);
            speedKillMapper.updateStock(id, speedKill.getNumber() - count);

            try {
                //插入订单
                orderMapper.insert(new Order(orderNo, id, 1));
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                //使用lua脚本释放锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

                RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
                Object result = redisTemplate.execute(redisScript, Collections.singletonList(redisLockKey), redisLockValue);
                if(!Long.valueOf(1).equals(result)) {
                    throw new RuntimeException("Redis解锁异常!"); //事务回滚
                }
            }

        }
    }
```

分布式计数器的应用场景：接口限流，比如一分钟1000次，在多节点的时候需要用到分布式计数器。需要保证两个条件：1. 线程安全。2. 速度够快。

回到题目，预设一个库存数量，每次下单库存减1，可利用redis自减操作decr key，当结果值大于等于0时，库存有效，当结果值小于等于-1时，超过库存，抛出异常。

```java
 /***
     * 基于redis实现一个分布式计数器,用于扣减库存,
     */
    @Transactional
    public void updateStockByDecr(String orderNo, long id) {
        String redisLockKey = "product_" + id;
        orderMapper.insert(new Order(orderNo, id, 1));
        Long decrement = redisTemplate.opsForValue().decrement(redisLockKey);
        if(decrement < 0L) {
            throw new RuntimeException("库存不足..;");
        }
    }
```

### 基于 Redis 的 PubSub 实现订单异步处理

第一个客户端

```shell
redis 127.0.0.1:6379**>** SUBSCRIBE runoobChat

Reading messages... **(**press Ctrl-C to quit**)**
1**)** "subscribe"
2**)** "redisChat"
3**)** **(**integer**)** 1
```

第二个客户端

```shell
redis 127.0.0.1:6379> PUBLISH runoobChat "Redis PUBLISH test"
(integer) 1

redis 127.0.0.1:6379> PUBLISH runoobChat "Learn redis by runoob.com"
(integer) 1

# 订阅者的客户端会显示如下消息
 1) "message"
2) "runoobChat"
3) "Redis PUBLISH test"
 1) "message"
2) "runoobChat"
3) "Learn redis by runoob.com"
```

代码实现：

```java
@Service
@Slf4j
public class RedisPubSub {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private ChannelTopic topic = new ChannelTopic("/redis/order");

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
            //添加订单
        }

        @Bean
        public MessageListenerAdapter listener(Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer,
                                               MessageSubscriber subscriber) {
            //这里使用了委托模式，将委托对象delegate作为参数传入，实际调用的是delegate的onMessage方法。
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
```





