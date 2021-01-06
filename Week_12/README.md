# 第十二周 分布式缓存——集群

## Redis主从复制

### 复制原理

**Slave 启动成功连接到 Master 后会发送一个 sync 同步命令。** （**可类比mysql的主从同步，原理基本一致**）

Master 接收到命令后，启动后台的存盘进程，同时收集所有接收到的用于修改数据集的命令，在后台进程执行完毕后，master 将传送整个数据文件到 slave ，并完成一次完全同步。

**全量复制：**Slave 服务在接收到数据库文件后，将其存盘并加载到内存中。

**增量复制：** Master 继续将新的所有收集到的修改命令一次传给 slave，完成同步。

但是只要重新连接 master ，一次完全同步（全量复制）将被自动执行。我们的数据一定可以在从机中看到。

命令行添加（临时的，重启后失效）：

```shell
slaveof host port
```

配置文件（永久）

```shell
# replicaof <masterip> <masterport>			# 这里配置
# masterauth <master-password>
```

主从同步的缺点：

不是高可用，当master宕机过后，需要手动将一台服务器切换为主服务器，期间服务不可用。

docker-compose部署集群

```yaml
version: '3'
services:
  master:
    image: redis
    container_name: redis-master
    command: redis-server --requirepass redis_pwd  --masterauth redis_pwd
    ports:
      - 6380:6379
  slave1:
    image: redis
    container_name: redis-slave-1
    ports:
      - 6381:6379
    command:  redis-server --slaveof redis-master 6379 --requirepass redis_pwd --masterauth redis_pwd
  slave2:
    image: redis
    container_name: redis-slave-2
    ports:
      - 6382:6379
    command: redis-server --slaveof redis-master 6379 --requirepass redis_pwd --masterauth redis_pwd
```



--------

## Redis哨兵模式

### 原理

主从模式一旦主机出问题，服务就不可用了。那么哨兵模式就是要达到容错failover的目的。为什么叫哨兵模式？顾名思义通过设置一个哨兵来监控所有redis的健康状态（发送指令PING-PONG），当哨兵检测到master宕机不可用时，自动将一个slave切换成master，并通过发布订阅模式告知其他节点切换主机。这样的好处是，我们只需要与哨兵打交道，至于监控和容错交给哨兵去执行就好了。

但是单个哨兵有单点问题，这里我们可以设置多个哨兵，相互监控。

假设主服务器宕机了，哨兵1先检测到这个结果，系统并不会马上进行 failover 过程，仅仅是哨兵 1 主观认为主服务器不可用，这个现象称之为**主观下线**。当后面的哨兵也检测到主服务器不可用，并且数量达到一定值时，那么哨兵之间就会进行一次投票，投票的结果由一个哨兵发起，进行 failover 【故障转移】。切换成功后，就会通过发布订阅模式，让各个哨兵把自己监控的从服务器实现切换主机，这个过程称之为**客观下线**。

需要注意的是哨兵不同于redis服务，它们是不同的程序。

![img](https://s1.ax1x.com/2020/05/15/YypgWn.png)

先不熟好1主2从的redis集群，然后再部署3个哨兵节点（注意哨兵没有主从之分），他们的任务都是监控redis集群。

1. **配置**

在sentinel.conf中配置：

```yaml
# port <sentinel-port>
port 26379
# sentinel monitor <master-name> <ip> <redis-port> <quorum> 权重
sentinel monitor mymaster 127.0.0.1 6380 2
# 当master服务器配置了密码时需要配置
sentinel auth-pass mymaster 123456
```

多机环境下，每个哨兵的配置都是相同的。需要注意的是哨兵只配置了master服务器的地址和端口，并没有配置从服务器的信息，但是上图中哨兵确实对所有redis服务器进行监控的。**那么哨兵是如何感知到从服务器的状态变更的呢？**

```shell
127.0.0.1:6379> info replication
# Replication
role:master
connected_slaves:2
slave0:ip=172.21.0.3,port=6379,state=online,offset=179,lag=1
slave1:ip=172.21.0.4,port=6379,state=online,offset=179,lag=1
```

查看在master的集群状态时可以发现，master维护了其slave的节点信息，并且通过PING-PONG机制对其从节点进行了健康检测，所以master节点就拥有从节点的信息，哨兵不需要单独配置从节点的信息，而是通过master节点间接获取了从节点信息。

2. **启动Sentinel**

```shell
redis-sentinel sentinel.conf
```

如果关闭master节点，会发生什么？

我们手动关闭Master 之后，sentinel 在监听master 确实是断线了之后，将会开始计算权值，然后重新分配主服务器。这时如果master重新上线，也只能是从节点。

**sentinel也没有配置其他sentinel的信息，它又是如何发现其他sentinel的呢？**原理与主从差不多，都是通过发布订阅模式进行获取。

### <img src="/Users/lijiajun/Downloads/380271-20181108161019953-1764260447.png" alt="380271-20181108161019953-1764260447"  />Sentinel的工作方式

1)：每个Sentinel以每秒钟一次的频率向它所知的Master，Slave以及其他 Sentinel 实例发送一个 PING 命令 
2)：如果一个实例（instance）距离最后一次有效回复 PING 命令的时间超过 down-after-milliseconds 选项所指定的值， 则这个实例会被 Sentinel 标记为主观下线。 
3)：如果一个Master被标记为主观下线，则正在监视这个Master的所有 Sentinel 要以每秒一次的频率确认Master的确进入了主观下线状态。 
4)：当有足够数量的 Sentinel（大于等于配置文件指定的值）在指定的时间范围内确认Master的确进入了主观下线状态， 则Master会被标记为客观下线 
5)：在一般情况下， 每个 Sentinel 会以每 10 秒一次的频率向它已知的所有Master，Slave发送 INFO 命令 
6)：当Master被 Sentinel 标记为客观下线时，Sentinel 向下线的 Master 的所有 Slave 发送 INFO 命令的频率会从 10 秒一次改为每秒一次 
7)：若没有足够数量的 Sentinel 同意 Master 已经下线， Master 的客观下线状态就会被移除。 
若 Master 重新向 Sentinel 的 PING 命令返回有效回复， Master 的主观下线状态就会被移除。

## Redis Cluster模式（分片模式）

主从集群的缺点是，每个节点保存了全量数据，因此总容量受制于单机容量，且不容易水平扩展。

分片模式：

将数据**按照某种规则**分散地存储在不同服务器上。

![img](https://img2018.cnblogs.com/blog/774371/201907/774371-20190704142443495-657525295.png)

Redis Cluster采用**一致性hash原理**进行数据分区。

![img](https://img2018.cnblogs.com/blog/1133627/201810/1133627-20181027173424090-1936846535.png)

主从复制从容量角度来说，还是单机。 

Redis Cluster通过一致性hash的方式，将数据分散到多个服务器节点：先设计 16384 个哈希槽，分配到多台redis-server。当需要Redis Cluster中存取一个 key时， Redis 客户端先对 key 使用 crc16 算法计算一个数值，然后对 16384 取模，这样每个 key 都会对应一个编号在 0-16383 之间的哈希槽，然后在 此槽对应的节点上操作。 

\> cluster-enabled yes 

注意： 

1、节点间使用gossip通信，规模<1000 

2、默认所有槽位可用，才提供服务 

3、一般会配合主从模式使用



## 作业 

### 配置 redis 的主从复制，sentinel 高可用，Cluster 集群。

1. 实现基于Lettuce和Redssion的Sentinel配置

   Lettuce操作单机redis和sentinel

   ```java
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
   ```

   Redisson操作单机redis和sentinel

   ```java
       public static void redissionClient() {
           Config config = new Config();
           config.useSingleServer()
                   .setAddress("redis://192.168.50.244:6380").setPassword("123456");
           RedissonClient client = Redisson.create(config);
           RMap<String, String> map = client.getMap("map");
           map.put("key1", "value1");
           System.out.println(map.get("key1"));
   
       }
   
       public static void redssionSentinel() {
           Config config = new Config();
           config.useSentinelServers().addSentinelAddress("redis://192.168.50.244:26379")
                   .setCheckSentinelsList(false).setMasterName("mymaster").setPassword("123456");
           RedissonClient client = Redisson.create(config);
           RMap<String, String> map = client.getMap("map");
           map.put("key1", "test");
           System.out.println(map.toString());
       } 
   ```

   

2. 实现springboot/spring data redis的sentinel配置

```yaml
# 只需要配置sentinel即可
spring:
  redis:
    sentinel:
      master: mymaster
      nodes: 192.168.50.244:26379,192.168.50.244:26380
    password: 123456
```

3. 使用jedis命令，使用java代码手动切换 redis 主从

```java
Jedis jedis1 = new Jedis("localhost", 6380);
jedis1.auth("123456");
System.out.println(jedis1.info("replication"));

Jedis jedis2 = new Jedis("localhost", 6381);
jedis2.auth("123456");
System.out.println(jedis2.info("replication"));

System.out.println(jedis2.slaveof("localhost",6380));
```

4. 使用C3的方式，使用java代码手动操作sentinel

```java

```

5. 	使用命令行配置Redis cluster:
   		1) 以cluster方式启动redis-server
   		2) 用meet，添加cluster节点，确认集群节点数目
   		3) 分配槽位，确认分配成功
   		4) 测试简单的get/set是否成功

**命令行配置redis cluster步骤：**

* 搭建一个3个节点的集群，目录结构如下

```shell
(base) lijiajundeMBP:redis-cluster lijiajun$ ls
7000			7001			7002			docker-compose.yml	redis.conf
```

* 启动redis集群需要的配置

```shell
port 7000 #每个节点端口号不通，并放置到相应文件夹下，docker挂载使用
#daemonize yes
bind 0.0.0.0
cluster-enabled yes
cluster-config-file nodes.conf
cluster-node-timeout 5000
appendonly yes
```

* Docker-compose文件：

```yaml
version: '3'

services:
 redis1:
  image: redis:4.0
  container_name: redis_cluster_1
  ports:
   - 7000:7000
  volumes:
   - /Users/lijiajun/docker/redis-cluster/7000:/usr/local/etc/redis
  entrypoint: redis-server /usr/local/etc/redis/redis_7000.conf

 redis2:
  image: redis:4.0
  container_name: redis_cluster_2
  ports:
   - 7001:7001
  volumes:
   - /Users/lijiajun/docker/redis-cluster/7001:/usr/local/etc/redis
  entrypoint: redis-server /usr/local/etc/redis/redis_7001.conf

 redis3:
  image: redis:4.0
  container_name: redis_cluster_3
  ports:
   - 7002:7002
  volumes:
   - /Users/lijiajun/docker/redis-cluster/7002:/usr/local/etc/redis
  entrypoint: redis-server /usr/local/etc/redis/redis_7002.conf
```

* Docker exec进入7000的容器，执行meet操作，让以上三个redis服务器相互感知。（只需要在7000上meet 7001，7002）

```shell
# redis-cli -p 7000 cluster meet 172.23.0.3 7001
# redis-cli -p 7000 cluster meet 172.23.0.4 7002
# redis-cli -p 7000 cluste info  查看集群状态
cluster_state:fail  #集群状态为fail，因为还没有为3个节点分配槽位
cluster_slots_assigned:0
cluster_slots_ok:0
cluster_slots_pfail:0
cluster_slots_fail:0
cluster_known_nodes:3
cluster_size:0
cluster_current_epoch:2
cluster_my_epoch:0
cluster_stats_messages_ping_sent:201
cluster_stats_messages_pong_sent:193
cluster_stats_messages_meet_sent:2
cluster_stats_messages_sent:396
cluster_stats_messages_ping_received:193
cluster_stats_messages_pong_received:203
cluster_stats_messages_received:396
```

* 为三个节点分配槽位，key值近过客户端进行CRC16hash运算得到的值进行取`mod`16384运算会得到一个槽值（0-16383），从而找到对应的redis节点，多个节点要瓜分完16384个槽位。

  `ps`:`CRC16`算法产生的hash值有16bit，该算法可以产生2^16-=65536个值。换句话说，值是分布在0~65535之间。那作者在做`mod`运算的时候，为什么不`mod`65536，而选择`mod`16384？因为每秒钟，redis节点需要发送一定数量的ping消息作为心跳包，如果槽位为65536，这个ping消息的消息头太大了，浪费带宽。

  执行以下命令：

```shell
# redis-cli -p 7000 cluster addslots {0..5461}
# redis-cli -h 172.23.0.3 -p 7001 cluster addslots {5462..10922}
# redis-cli -h 172.23.0.4 -p 7002 cluster addslots {10923..16383}
# redis-cli -p 7000 cluster info
cluster_state:ok  #可以看到此时集群状态已经OK
cluster_slots_assigned:16384
cluster_slots_ok:16384
cluster_slots_pfail:0
cluster_slots_fail:0
cluster_known_nodes:3
cluster_size:3
cluster_current_epoch:2
cluster_my_epoch:0
cluster_stats_messages_ping_sent:776
cluster_stats_messages_pong_sent:738
cluster_stats_messages_meet_sent:2
cluster_stats_messages_sent:1516
cluster_stats_messages_ping_received:738
cluster_stats_messages_pong_received:778
cluster_stats_messages_received:1516
# redis-cli -c -p 7000 #注意客户端操作时，需要加上-c参数集群模式，不然MOVED到其他节点的时候会报错
```

* 使用java代码测试集群

```java
   private static JedisCluster createJedisCluster() {
        JedisCluster jedisCluster = null;
                // 添加集群的服务节点Set集合
        Set<HostAndPort> hostAndPortsSet = new HashSet<HostAndPort>();
        // 添加节点
        hostAndPortsSet.add(new HostAndPort("localhost", 7000));
        hostAndPortsSet.add(new HostAndPort("localhost", 7001));
        hostAndPortsSet.add(new HostAndPort("localhost", 7002));

        // hostAndPortsSet.add(new HostAndPort("127.0.0.1", 6381));

        // Jedis连接池配置
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        // 最大空闲连接数, 默认8个
        jedisPoolConfig.setMaxIdle(12);
        // 最大连接数, 默认8个
        jedisPoolConfig.setMaxTotal(16);
        //最小空闲连接数, 默认0
        jedisPoolConfig.setMinIdle(4);
        // 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        jedisPoolConfig.setMaxWaitMillis(2000); // 设置2秒
        //对拿到的connection进行validateObject校验
        jedisPoolConfig.setTestOnBorrow(true);
        jedisCluster = new JedisCluster(hostAndPortsSet, jedisPoolConfig);
        return jedisCluster;
    }
```





参考：

https://segmentfault.com/a/1190000002680804

https://www.cnblogs.com/JulianHuang/p/12650721.html

https://www.cnblogs.com/williamjie/p/11132211.html
