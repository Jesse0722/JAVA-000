# 数据库二——分库分表、数据迁移与数据一致性

## 作业1 设计对前面的订单表数据进行水平分库分表，拆分 2 个库，每个库 16 张表。并在新结构在演示常见的增删改查操作。代码、sql 和配置文件，上传到 Github。

1. 数据库sql

```mysql
CREATE TABLE `order` (
     `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
     `no` varchar(120) NOT NULL COMMENT '订单号',
     `user_id` bigint NOT NULL COMMENT '用户id',
     `product_id` bigint NOT NULL COMMENT '秒杀商品id',
     `status` int(4) NOT NULL COMMENT '秒杀状态',
     `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     PRIMARY KEY (`id`),
     UNIQUE KEY `idx_no` (`no`),
  	 KEY `idex_user_d` (`user_id`),
     KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8 COMMENT='订单表';
```

2. config-sharding.yaml 配置

```yml
schemaName: order_db

dataSourceCommon:
 username: admin
 password: 123456
 connectionTimeoutMilliseconds: 30000
 idleTimeoutMilliseconds: 60000
 maxLifetimeMilliseconds: 1800000
 maxPoolSize: 50
 minPoolSize: 1
 maintenanceIntervalMilliseconds: 30000

dataSources:
 ds_0:
   url: jdbc:mysql://127.0.0.1:3306/ds_0?serverTimezone=UTC&useSSL=false
 ds_1:
   url: jdbc:mysql://127.0.0.1:3306/ds_1?serverTimezone=UTC&useSSL=false

rules:
- !SHARDING
 tables:
   t_order:
     actualDataNodes: ds_${0..1}.t_order_${0..15}
     tableStrategy:
       standard:
         shardingColumn: id
         shardingAlgorithmName: t_order_inline
     keyGenerateStrategy:
       column: id
       keyGeneratorName: snowflake

 bindingTables:
   - t_order
 defaultDatabaseStrategy:
   standard:
     shardingColumn: user_id
     shardingAlgorithmName: database_inline
 defaultTableStrategy:
   none:
 
 shardingAlgorithms:
   database_inline:
     type: INLINE
     props:
       algorithm-expression: ds_${user_id % 2}
   t_order_inline:
     type: INLINE
     props:
       algorithm-expression: t_order_${id % 16}

 
 keyGenerators:
   snowflake:
     type: SNOWFLAKE
     props:
       worker-id: 123

```

3. springboot配置

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/order_db?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=CTT&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
```

4. 基于mybatis的增删改查代码

```java
@SpringBootTest
@RunWith(SpringRunner.class)
class OrderServiceTest {
    @Autowired
    private OrderMapper orderMapper;

    @Test
    void insert() {
        Order order = new Order("xasdasdaqsfafa", 12345567,123123, 1);
        orderMapper.insert(order);
    }

    @Test
    void delete() {
        orderMapper.delete(23956);    }

    @Test
    void select() {
        System.out.println(orderMapper.get(23958,123456));
    }

    @Test
    void getByUserId() {
        System.out.println(orderMapper.getByUserId(123456L));
    }
    
}
```

## 作业2 基于 hmily TCC 或 ShardingSphere 的 Atomikos XA 实现一个简单的分布式事务应用 demo，提交到 Github。

### 环境配置

启动两个MySQL5.7的docker镜像（8.0的一直报事务相关的错的，不知啥原因，换成5.7就好了），下面命令直接复制运行即可：

```
# 启动两个mysql
docker run --name mysql11 -p 3311:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_ROOT_HOST=% -d mysql:5.7
docker run --name mysql12 -p 3312:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_ROOT_HOST=% -d mysql:5.7
﻿
# 在11上创建数据库demo_ds_0，运行下面的SQL语句初始化数据库和表
docker exec -ti mysql11 mysql -u root -p
﻿
create database demo_ds_0;
use demo_ds_0;
CREATE TABLE IF NOT EXISTS t_order_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, PRIMARY KEY (order_id));
    
﻿
# 在12上创建数据库demo_ds_1，运行下面的SQL语句初始化数据库和表
docker exec -ti mysql12 mysql -u root -p
﻿
create database demo_ds_1;
use demo_ds_1;
CREATE TABLE IF NOT EXISTS t_order_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, PRIMARY KEY (order_id));
```

### 程序配置

#### Maven依赖

    核心依赖下面几个：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
﻿
    <groupId>com.example</groupId>
    <artifactId>demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>transaction-2pc-xa-raw-jdbc-example</name>
    <description>Demo project for Spring Boot</description>
  
    <properties>
        <java.version>1.8</java.version>
    </properties>
﻿
    <dependencies>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId> <!-- Use 'netty-all' for 4.0 or above -->
            <version>8.0.14</version>
        </dependency>
﻿
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-jdbc-core</artifactId>
            <version>5.0.0-alpha</version>
        </dependency>
﻿
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-transaction-xa-core</artifactId>
            <version>5.0.0-alpha</version>
        </dependency>
﻿
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>2.2.5</version>
        </dependency>
    </dependencies>
﻿
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>7</source>
                    <target>7</target>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

### shardingSphere数据库配置文件

    如上面的docker数据库配置，这里设置了两个数据库，各自有两张表

```yaml
dataSources:
  ds_0: !!com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3311/demo_ds_0?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: root
    autoCommit: false
  ds_1: !!com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3312/demo_ds_1?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: root
    autoCommit: false
﻿
rules:
- !SHARDING
  tables:
    t_order:
      actualDataNodes: ds_${0..1}.t_order_${0..1}
      databaseStrategy:
        standard:
          shardingColumn: user_id
          shardingAlgorithmName: database_inline
      tableStrategy:
        standard:
          shardingColumn: order_id
          shardingAlgorithmName: t_order_inline
  bindingTables:
    - t_order
﻿
  shardingAlgorithms:
    database_inline:
      type: INLINE
      props:
        algorithm-expression: ds_${user_id % 2}
    t_order_inline:
      type: INLINE
      props:
        algorithm-expression: t_order_${order_id % 2}
﻿
props:
  sql-show: true
```

### 主程序

    暴力直接的原生测试，代码如下：

```java
package com.example.demo;
﻿
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
﻿
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
﻿
public class Transaction2pcXaRawJdbcExampleApplication {
﻿
    /**
     * 第一次插入数据正常运行成功
     * 第二次插入数据由于主键冲突，导致回滚
     */
    public static void main(String[] args) throws IOException, SQLException {
        DataSource dataSource = getShardingDatasource();
        cleanupData(dataSource);
﻿
        TransactionTypeHolder.set(TransactionType.XA);
﻿
        Connection conn = dataSource.getConnection();
        String sql = "insert into t_order (user_id, order_id) VALUES (?, ?);";
﻿
        System.out.println("First XA Start insert data");
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (int i = 1; i < 11; i++) {
                statement.setLong(1, i);
                statement.setLong(2, i);
                statement.executeUpdate();
            }
            conn.commit();
        }
﻿
        System.out.println("First XA insert successful");
﻿
        // 设置id+5，如果设置XA事务成功，则所有的数据都不会插入
        // 设置id+5，如果设置XA事务不成功，则id大于10的数据就会插入到数据库
        // 程序运行完毕后，查看数据库，没有id大于10的数据，所以XA设置成功
        System.out.println("Second XA Start insert data");
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (int i = 1; i < 11; i++) {
                statement.setLong(1, i+5);
                statement.setLong(2, i+5);
                statement.executeUpdate();
            }
            conn.commit();
        } catch (Exception e) {
            System.out.println("Second XA insert failed");
            conn.rollback();
        } finally {
            conn.close();
        }
        System.out.println("Second XA insert successful");
    }
﻿
    private static void cleanupData(DataSource dataSource) {
        System.out.println("Delete all Data");
        try (Connection conn = dataSource.getConnection(); Statement statement = conn.createStatement()) {
            statement.execute("delete from t_order;");
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Delete all Data successful");
    }
﻿
    /**
     * 生成DataSource，文件路径自行替换
     * @return
     * @throws IOException
     * @throws SQLException
     */
    static private DataSource getShardingDatasource() throws IOException, SQLException {
        String fileName = "F:\\Code\\Java\\JAVA-000\\homework\\shardingSphere-jdbc-example\\transaction-example\\transaction-2pc-xa-raw-jdbc-example\\src\\main\\resources\\sharding-databases-tables.yaml";
        File yamlFile = new File(fileName);
        return YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
    }
﻿
}
```



参考：https://blog.csdn.net/zhuyu19911016520/article/details/90051340

