# 作业

1. 使用Java的动态代理，实现一个简单的AOP。

Java里的动态代理都是用过Proxy.newProxyInstance(ClassLoader classLoader, Class[] interfaces, InvocationHandler)在程序运行时通过反射创建出来的。可以通过反射创建对象的反射类，Class和Constructor。

```java
IHello o = (IHello) Proxy.newProxyInstance(handler.getClass().getClassLoader(), new Class[]{Log.class, IHello.class}, handler);
```

这里的参数有三个：

 * classLoader 类加载对象。
 * interfaces是需要代理的接口类对象
 * 真正进行方法调用的处理器，需要实现其invoke(Proxy proxy, Method method,  Object[] args) 方法。

这里解释一下为什么第二个参数需要传入一个接口数组，这是因为我们需要代理的对象有可能实现了多个接口，而Proxy只能通过接口来代理，比如一个类Logger实现了两个接口Log1和Log2，我们需要对这个Logger进行动态代理，并且对Log1的log1方法和Log2的log2方法分别进行增强,这是就需要我们把两个代理接口都作为参数传入，否则不能进行代理。不过大多数情况只代理一个接口的特定方法。

再来说说代理的处理器InvocationHandler，他只有一个invoke方法，用来执行增强后的方法，其三个参数所代表的意思：

- proxy是代理出来的对象。他是Class[] interfaces传入的这几个接口的实例。
- method调用的方法引用，比如proxy.log()
- args对应方法的参数

invoke方法是一个增强方法，也就意味着他是在原来的一个类的方法上进行增强，所以这里在实例化InvocationHandler的时候需要使用一个构造参数将要代理的对象传入进来，在invoke里面进行调用增强。

```java
@Override
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    //Proxy Before...
    Object object =  method.invoke(targetObject, args);
    //Proxy After...
    return object;
}
```

以上是java动态代理基础，下面来它来实现一个简单的AOP。



2. 写代码实现Spring Bean的装配，方式越多越好(XML、Annotation都可以),提 交到Github。

一共有三种方式：

 - Xml方式

 - Java方式
 - 自动装配

3. 实现一个 Spring XML 自定义配置，配置一组 Bean，例如：Student/Klass/Schoo。

   完成一个spring的自定义配置一般需要以下5个步骤：

   1. 设计配置属性和JavaBean
   2. 编写XSD文件 全称就是 XML Schema 它就是校验XML，定义了一些列的语法来规范XML
   3. 编写NamespaceHandler和BeanDefinitionParser完成解析工作
   4. 编写spring.handlers和spring.schemas串联起所有部件
   5. 在Bean文件中应用

4. 给前面课程提供的 Student/Klass/School 实现自动配置和 Starter。

   思路：

   * 创意见一个XXXXAutoConfiguration的配置类，在里面定义好要创建的Bean。

   * 在spring.factories文件将第一步定义好的自定配置类的路径配置进去。

     这样程序启动的时候会去扫描spring.factories这个文件，并自动装配bean，然后直接使用就可以了。

   实现步骤：

   1. 新建一个starter项目，引入spring-boot-starter  --> 编写AutoConfiguration 需要添加的注解：

   ```java
   @EnableConfigurationProperties(SchoolProperties.class) //将@ConfigurationProperties 的类进行注入。
   @Configuration //声明为配置类
   @ConditionalOnClass(School.class) //条件控制
   @ConditionalOnProperty(prefix = "school", value = "enable", havingValue = "true") //条件控制
   @PropertySource("classpath:application.properties")
   ```

   2. 实现配置

      使用@ConfigurationProperties注解标注一个

      ```
      @ConfigurationProperties(prefix = "school")
      @Data
      public class SchoolProperties {
          public List<Integer> studentIds;
      
          public List<String> studentNames;
      
          public List<Integer> klassIds;
      ```

   

   3. 在AutoXXXConfiguration定义要装配的Bean

   4. resources META-INF下面新建spring.factories，将自动配置的类添加进去

      ```properties
      org.springframework.boot.autoconfigure.EnableAutoConfiguration=io.github.jesse0722.springDemo.jesse0722.starterDemo.SchoolAutoConfiguration
      ```

4. 研究一下 JDBC 接口和数据库连接池，掌握它们的设计和用法：
   1）使用 JDBC 原生接口，实现数据库的增删改查操作。

   2）使用事务，PrepareStatement 方式，批处理方式，改进上述操作。
   3）配置 Hikari 连接池，改进上述操作。提交代码到 Github。

   **查询一个sql语句的步骤**

   * 注册驱动程序

     Class.forName(“com.mysql.jdbc.Driver”);

     利用反射将驱动（也就是mysql-connector-java这个包下的）强行加载到内存

   * 获得数据库连接对象Connection

     Connection  conn = DriverManager.getConnection(url, username,password)

   * 获得SQL语句的执行对象

      PreparedStatement ps = conn.prepareStatement(“select …”)  //为什么用PreparedStatement

   * 给占位符设置参数

   ​     ps.setString(index,str) //给参数赋值，索引从1开始

   * 执行sql语句，并返回结果集ResultSet

   ​     ResultSet rs = ps.executeQuery()

   * 对查询结果进行转换处理并将处理结果返回

   * 释放相关资源（关闭Connection，关闭Statement，关闭ResultSet）

