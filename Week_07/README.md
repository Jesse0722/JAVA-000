

## Week07 学习笔记及客后作业

## 学习笔记

### 1. 事务

事务(Transaction)，一般是指要做的或所做的事情。在计算机术语中是指访问并可能更新数据库中各种数据项的一个程序执行单元(unit)。事务由事务开始(begin transaction)和事务结束(end transaction)之间执行的全体操作组成。在关系数据库中，一个事务可以是一组SQL语句或整个程序。

#### 为什么要有事务

一个数据库事务通常包含对数据库进行读或写的一个操作序列。它的存在包含有以下两个目的：

1. 为数据库操作提供了一个从失败中恢复到正常状态的方法，同时提供了数据库在异常状态下仍能保持一致性的方法。
2. 当多个应用程序在并发访问数据库时，可以在这些应用程序之间提供一个隔离方法，保证彼此的操作互相干扰。

**事务特性ACID:** 

•Atomicity: 原子性, 一次事务中的操作要么全部成功, 要么全部失败。 

•Consistency: 一致性, 跨表、跨行、跨事务, 数据库始终保持一致状态。 

•Isolation: 隔离性, 可见性, 保护事务不会互相干扰, 包含4种隔离级别。 

•Durability:, 持久性, 事务提交成功后,不会丢数据。如电源故障。(通过向磁盘谢日志的方式来保证。)

InnoDB: 

双写缓冲区、故障恢复、操作系统、fsync() 、磁盘存储、缓存、UPS、网络、备份策略 ……

### 2. **脏读、幻读、不可重复读的概念**

Java中，两个线程对一个共享变量的操作，如果不做同步，会导致线程安全问题。

这里的线程安全问题可以类比数据库中两个事务交叉执行，所产生的脏读、幻读问题。

**1. 脏读**

所谓脏读是指一个事务中访问到了另外一个事务未提交的数据，如下图

<img src="/Users/lijiajun/Library/Application Support/typora-user-images/image-20201128154755672.png" alt="image-20201128154755672" style="zoom:50%;" />

如果会话 2 更新 age 为 10，但是在 commit 之前，会话 1 希望得到 age，那么会获得的值就是更新前的值。或者如果会话        2 更新了值但是执行了 rollback，而会话 1 拿到的仍是 10。这就是脏读。

**2. 幻读**

一个事务读取2次，得到的记录条数不一致：

<img src="/Users/lijiajun/Library/Application Support/typora-user-images/image-20201128155228397.png" alt="image-20201128155228397" style="zoom:50%;" />



上图很明显的表示了这个情况，由于在会话 1 之间插入了一个新的值，所以得到的两次数据就不一样了。

**3. 不可重复读**

一个事务读取同一条记录2次，得到的结果不一致：

<img src="/Users/lijiajun/Library/Application Support/typora-user-images/image-20201128155407217.png" alt="image-20201128155407217" style="zoom:50%;" />

### 3. 排他锁，共享锁

排它锁（Exclusive），又称为X 锁，写锁。

共享锁（Shared），又称为S 锁，读锁。

读写锁之间有以下的关系：

- 一个事务对数据对象O加了 S 锁，可以对 O进行读取操作，但是不能进行更新操作。加锁期间其它事务能对O 加 S 锁，但是不能加 X 锁。
- 一个事务对数据对象 O 加了 X 锁，就可以对 O 进行读取和更新。加锁期间其它事务不能对 O 加任何锁。

即读写锁之间的关系可以概括为：多读单写

### **4. 四种隔离级别**

首先为什么会有四种隔离级别？

正是为了解决以上的所产生的由于事务交叉执行所导致的问题。

<img src="/Users/lijiajun/Library/Application Support/typora-user-images/image-20201128153420031.png" alt="image-20201128153420031" style="zoom:50%;" />



1. 读未提交(Read Uncommitted)：解决更新丢失问题。如果一个事务已经开始写操作，那么其他事务则不允许同时进行写操作，但允许其他事务读此行数据。该隔离级别可以通过“排他写锁”实现，即事务需要对某些数据进行修改必须对这些数据加 X 锁，读数据不需要加 S 锁。

2. 读已提交(Read Committed)：解决了脏读问题。读取数据的事务允许其他事务继续访问该行数据，但是未提交的写事务将会禁止其他事务访问该行。这可以通过“瞬间共享读锁”和“排他写锁”实现， 即事务需要对某些数据进行修改必须对这些数据加 X 锁，读数据时需要加上 S 锁，当数据读取完成后立刻释放 S 锁，不用等到事务结束。

3. 可重复读取(Repeatable Read)：禁止不可重复读取和脏读取，但是有时可能出现幻读数据。读取数据的事务将会禁止写事务(但允许读事务)，写事务则禁止任何其他事务。Mysql默认使用该隔离级别。这可以通过“共享读锁”和“排他写锁”实现，即事务需要对某些数据进行修改必须对这些数据加 X 锁，读数据时需要加上 S 锁，当数据读取完成并不立刻释放 S 锁，而是等到事务结束后再释放。*

   * **mysql中默认的隔离级别**
   * 使用事务第一次读取时创建的快照。这样如果后面再select也会是第一次读取的结果。（select快照）
   * MVCC多版本控制技术。

   **锁:** 

   •使用唯一索引的唯一查询条件时, 只锁定查找到的索引记录, 不锁定间隙。 

   •其他查询条件, 会锁定扫描到的索引范围, 通过间隙锁或临键锁来阻止其他会话在这个 范围中插入值。 

   •可能的问题: InnoDB不能保证没有幻读, 需要加锁

4. 串行化(Serializable)：解决了幻读的问题的。提供严格的事务隔离。它要求事务序列化执行，事务只能一个接着一个地执行，不能并发执行。仅仅通过“行级锁”是无法实现事务序列化的，必须通过其他机制保证新插入的数据不会被刚执行查询操作的事务访问到。

事务的具体实现机制：

 对应命令rollback和commit的机制，使用undo log和redo log。也就是在刷盘之前写日志。

**MVCC**

* 使InnoDB支持一致性读: READ COMMITTED 和 REPEATABLE READ 。 

* 让查询不被阻塞、无需等待被其他事务持有的锁，这种技术手段可以增加并发性能。 

* InnoDB保留被修改行的旧版本。 

* 查询正在被其他事务更新的数据时，会读取更新之前的版本。 

* 每行数据都存在一个版本号, 每次更新时都更新该版本 

* 这种技术在数据库领域的使用并不普遍。 某些数据库, 以及某些MySQL存储引擎都不支持。

### 5. SQL优化

1. 数据库字段类型和引擎的选择

2. Where条件中避免敏感数据

3. 慢sql优化，查看慢sql日志

4. 添加索引

5. 范围查询避免加锁（gap锁）

6. B树和B+树的区别，B树所有节点都带有数据，B+树只有叶子结点有数据。

   聚簇索引：聚集索引是在索引存储结构上（一般是按照主键），保存了数据（innodb是在叶节点上）。

   二级索引：除聚簇索引外的索引，其节点保存的是主键id，需要根据主键id取聚簇索引查询数据。

   覆盖索引：如果一个索引包含了所需查询的字段的值，那么就是覆盖索引。特点是不需要回表。

   ![image-20201128172125874](/Users/lijiajun/Library/Application Support/typora-user-images/image-20201128172125874.png)

   ![image-20201128172149057](/Users/lijiajun/Library/Application Support/typora-user-images/image-20201128172149057.png)

   7. 数据主键应该是单调递增的，防止插入时的页分裂。

   8. 添加索引，在线上操作时会锁表，应该在用户使用较少的时候来操作。

   9. 为什么不用hash索引？要用B+树索引。因为hash索引不能进行范围查询，排序等操作。B+树索引非常适合磁盘的结构。 

   10. 为什么主键不能太大？主键长度太大，B+树主键放的数目就变少了。

   11. 主键查和index查谁快？

       聚集索引：主键id的索引。

       二级索引：单独结构，关联的不是数据，是主键，需要进行回表。

       所以大批量的查询尽量走聚集索引。

   12. 解释explain里面主要字段的含义。

       主要难以理解的是：

       **type**

       表示MySQL在表中找到所需行的方式，又称“访问类型”。

       常用的类型有： **ALL, index, range, ref, eq_ref, const, system, NULL（从左到右，性能从差到好）**

       * ALL：Full Table Scan， MySQL将遍历全表以找到匹配的行

       * index: Full Index Scan，index与ALL区别为index类型只遍历索引树

       * range: 索引范围扫描，对索引的扫描开始于某一点，返回匹配值域的行，常见于between、<、>等的查询

       * ref:  非唯一性索引扫描，返回匹配某个单独值的所有行。常见于使用非唯一索引即唯一索引的非唯一前缀进行的查找

       * eq_ref: 唯一性索引扫描，对于每个索引键，表中只有一条记录与之匹配。常见于主键或唯一索引扫描。

       * const、system: 当MySQL对查询某部分进行优化，并转换为一个常量时，使用这些类型访问。如将主键置于where列表中，MySQL就能将该查询转换为一个常量,system是const类型的特例，当查询的表只有一行的情况下，使用system

       * NULL: MySQL在优化过程中分解语句，执行时甚至不用访问表或索引，例如从一个索引列里选取最小值可以通过单独索引查找完成

       **Extra**

       * Using temporary：表示MySQL需要使用临时表来存储结果集，常见于排序和分组查询

       * Using filesort：MySQL中无法利用索引完成的排序操作称为“文件排序”

       * Using Index：表示直接访问索引就能够获取到所需要的数据（覆盖索引），不需要通过索引回表； 

       * Using Index Condition：在MySQL 5.6版本后加入的新特性（Index Condition Pushdown）;会先条件过滤索引，过滤完索引后找到所有符合索引条件的数据行，随后用 WHERE 子句中的其他条件去过滤这些数据行；

       * Using where: 表示MySQL服务器在存储引擎收到记录后进行“后过滤”（Post-filter）,如果查询未能使用索引，Using where的作用只是提醒我们MySQL将用where子句来过滤结果集。这个一般发生在MySQL服务器，而不是存储引擎层。一般发生在不能走索引扫描的情况下或者走索引扫描，但是有些查询条件不在索引当中的情况下。 

   13. 联合索引的命中规则是什么？*

       ```mysql
       INDEX `idx_a_b_c` (`a`,`b`,`c`)
       ```

        语句会创建一个有序数组的索引：分别是(1,a),(2,b),(3,c) 分别代表Seq_in_index ，Column_name，其结构如下

       <img src="/Users/lijiajun/Library/Application Support/typora-user-images/image-20201201164843623.png" alt="image-20201201164843623" style="zoom:50%;" />

       * 只要where条件中有a条件就会走索引，如果没有a，则不能走索引；（**最左原则**）

       * where条件中abc的顺序没有关系，执行的时候会进行优化排序。

         ```mysql
         select * from test where c = 'c' and a = 'a'; //能走索引
         ```

       * 如果select的字段都在联合索引的列之外(不能覆盖)，那么联合索引中只能命中Seq_in_index为1的列,如：

         ``` mysql
         select a,b,c from test where a = "a";//走覆盖索引
         select a,b,c,d from test where a = "a";//走索引，但不能覆盖
         ```

       通过expain我们可以看到是否走索引，什么类型的索引，以及是否是覆盖索引。

       ![image-20201128191515150](/Users/lijiajun/Library/Application Support/typora-user-images/image-20201128191515150.png)

   14. 修改表结构的危害？重建索引、锁表、抢占资源、主从延迟。

   15. 数据量太大时，需要增加字段怎么办？添加子表，进行关联。

   16. 大批量写入的优化：可以用PrepareStatement的addBatch操作,通过填充参数来执行。

   17. 范围数据更新的问题：一定要注意数据更新的范围，where条件一定要有，且尽量作用于主键上。注意gap锁。

   18. like模糊查询的问题：%like不走索引。所以%尽量写在like后面。

   19. 连接查询的问题：控制数据量的查询，避免笛卡尔积。

   20. 索引失效问题：NULL，not , not in ，函数等都不会走索引；减少or 和like操作，用in来代替or。

   21. mybatis 分页组建的坑：嵌套一个count函数；可以考虑非精确索引，将上一页最后一个数据的id，带到下一页的查询中，如：select * from order where id > 200 limit 20;

   22. 使用覆盖索引优化sql查询：通过建立联合索引，让联合索引的字段覆盖select查询的字段，这样就不需要回表查询，减少IO。

   23. 为什么不能用select *？有的text类型的大字段可能没必要进行传输; 失去了走覆盖索引的可能性。

   24. 悲观锁和乐观锁？悲观锁：select * for update; 乐观锁通过版本号，先select出来，update的时候带上版本号。

       

### 6.主从复制

**异步模式原理**

<img src="/Users/lijiajun/Library/Application Support/typora-user-images/image-20201129203511724.png" alt="image-20201129203511724" style="zoom:50%;" />

1、主节点必须启用二进制日志，记录任何修改了数据库数据的事件。
2、从节点开启一个线程（I/O Thread)把自己扮演成 mysql 的客户端，通过 mysql 协议，请求主节点的二进制日志文件中的事件
3、主节点启动一个线程（dump Thread），检查自己二进制日志中的事件，跟对方请求的位置对比，如果不带请求位置参数，则主节点就会从第一个日志文件中的第一个事件一个一个发送给从节点。
4、从节点接收到主节点发送过来的数据把它放置到中继日志（Relay log）文件中。并记录该次请求到主节点的具体哪一个二进制日志文件内部的哪一个位置（主节点中的二进制文件会有多个，在后面详细讲解）。
5、从节点启动另外一个线程（sql Thread ），把 Relay log 中的事件读取出来，并在本地再执行一次



<img src="/Users/lijiajun/Library/Application Support/typora-user-images/image-20201129202856351.png" alt="image-20201129202856351" style="zoom:50%;" />



MySQL中有六种日志文件，
分别是：重做日志（redo log）、回滚日志（undo log）、二进制日志（binlog）、错误日志（errorlog）、慢查询日志（slow query log）、一般查询日志（general log），中继日志（relay log）。
其中重做日志和回滚日志与事务操作息息相关，二进制日志也与事务操作有一定的关系，这三种日志，对理解MySQL中的事务操作有着重要的意义。

mac环境docker搭建主从：https://www.cnblogs.com/zendwang/p/docker-install-mysql-master-slave.html

## 课后作业

### 1.（必做）按自己设计的表结构，插入 100 万订单模拟数据，测试不同方式的插入效率

建表：

```mysql
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
     `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
     `no` varchar(120) NOT NULL COMMENT '订单号',
     `product_id` bigint NOT NULL COMMENT '秒杀商品id',
     `status` int(4) NOT NULL COMMENT '秒杀状态',
     `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     PRIMARY KEY (`id`),
     UNIQUE KEY `idx_no` (`no`),
     KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8 COMMENT='订单表';
```

**方式一 单线程单条执行。**

```java
public void insertSingleThread(int number) {
    for (int i = 0; i < number; i++) {
        Order order = new Order(UUID.randomUUID().toString(), (long) (Math.random() * 100), 1);
        orderMapper.insert(order);
    }
}
```

 Spend Time:352810

**方式二：多线程单线执行。**

```java
public void insertMultiThread(int number) {

    for (int i = 0; i < number; i++) {
        Order order = new Order(UUID.randomUUID().toString(), (long) (Math.random() * 100), 1);
        executorService.submit(()-> orderMapper.insert(order));
    }
}
```

Spend Time:152340

**方式三：单线程group insert多条执行。**

```java
public void insertBatchGroup(final int number) {
    int orderNum = number;
    while(orderNum > 0) {
        int initSize = orderNum > 500 ? 500 : orderNum;
        List<Order> orders = new ArrayList<>(initSize);
        for (int i = 0; i < initSize; i++) {
            Order order = new Order(UUID.randomUUID().toString(), (long) (Math.random() * 100), 1);
            orders.add(order);
        }
        orderMapper.insertBatch(orders);
        orderNum = orderNum - 500;
    }
}
```

Spend Time:59240

效果 最好

### 2.（必做）使用AOP实现读写分离。

见代码。

### 3.(必做）使用框架实现读写分离。

见代码。
