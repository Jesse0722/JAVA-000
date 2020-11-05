# 学习笔记

## Java NIO

### NIO三大概念

1. Channel

   Channel可类比于BIO中的Socket，这里的Channel要比Socket更加具体，可以把它比作某种具体的交通工具，比如汽车或者高铁，它是具体的通讯载体，是双向性的，一端写入，另外一端读取，反之依然。根据通信形式的不同，其实现有以下几种：

   * FileChannel
   * DatagramChannel
   * SocketChannel
   * ServerSocketChannel



2. Selector

   可以把Selector比作一个车站的车辆运行调度系统，它将负责监控每辆车的当前运行状态，是已经出站，还是在路上等。也就是它可以轮训每个Channel的状态。这样的好处是，一个selector可以同时监控多个channel，对其进行有效的管理。

   ![img](http://ifeve.com/wp-content/uploads/2013/06/overview-channels-buffers.png)

3. Buffer

   Buffer用于和channel通道进行交互，数据是从channel读入缓冲区，从缓冲区写入通道的。缓冲区本质上是一块可以写入数据，然后可以从中读取数据的内存。这块内存被包装成NIO Buffer对象，并提供了一组方法，用来方便的访问该块内存。注意Buffer不代表缓冲区，它是操作缓冲区这块内存的对象。可把它类比为列车上的座位，用来承载具体的数据。

   

   Buffer的工作方式

   为了理解Buffer的工作原理，需要熟悉它的三个属性：

   - capacity   
   - position 
   - limit

   position和limit的含义取决于Buffer处在读模式还是写模式。不管Buffer处在什么模式，capacity的含义总是一样的。

   这里有一个关于capacity，position和limit在读写模式中的说明，详细的解释在插图后面。

   ![img](http://ifeve.com/wp-content/uploads/2013/06/buffers-modes.png)

   #### capacity

   作为一个内存块，Buffer有一个固定的大小值，也叫“capacity”.你只能往里写capacity个byte、long，char等类型。一旦Buffer满了，需要将其清空（通过读数据或者清除数据）才能继续写数据往里写数据。

   #### position

   当你写数据到Buffer中时，position表示当前的位置。初始的position值为0.当一个byte、long等数据写到Buffer后， position会向前移动到下一个可插入数据的Buffer单元。position最大可为capacity – 1.

   当读取数据时，也是从某个特定位置读。当将Buffer从写模式切换到读模式，position会被重置为0. 当从Buffer的position处读取数据时，position向前移动到下一个可读的位置。

   #### limit

   在写模式下，Buffer的limit表示你最多能往Buffer里写多少数据。 写模式下，limit等于Buffer的capacity。

   当切换Buffer到读模式时， limit表示你最多能读到多少数据。因此，当切换Buffer到读模式时，limit会被设置成写模式下的position值。换句话说，你能读到之前写入的所有数据（limit被设置成已写数据的数量，这个值在写模式下就是position）



### selector多路复用demo

```java
package nio_demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Lijiajun
 * @date 2020/10/30 15:42
 */
public class SelectorSingleThreadServer {
    private ServerSocketChannel server = null;
    private Selector selector = null;

    public void initServer(int port){
        try {
            selector = Selector.open();
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.socket().bind(new InetSocketAddress(port));
            server.register(selector, SelectionKey.OP_ACCEPT); // 注册到selector,状态为OP_ACCEPT，标示可以accept一个connection
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException {
        System.out.println("Server started。。。");
        while(true) {
            while (selector.select(0) > 0) { //调用select方法寻问内核是否有channel准备好IO操作
                Set<SelectionKey> selectionKeys = selector.selectedKeys(); // 从多路复用器 取出有效的key
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) { //遍历可操作的channel事件	
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) { //如果可以建立连接
                        acceptHandler(key);
                    } else if (key.isReadable()) { //如果可读
                        readHandler(key); //对可读的客户端通道进行数据读取
                    }
                    iterator.remove();
                }
            }
        }
    }

    public void acceptHandler(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel(); //获取服务端channel
        SocketChannel client = ssc.accept(); //获取一个客户端连接
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(4)); //注册客户端,channel与buffer绑定
        System.out.println("New client:" + client.getRemoteAddress());
    }

    /***
     * 如何读取缓冲区数据
     * @param key
     * @throws IOException
     */
    public void readHandler(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel(); //获取客户端channel
        ByteBuffer buffer = (ByteBuffer) key.attachment(); //获取buffer
        //先清空buffer
        buffer.clear();
        while(true) {
            int n = client.read(buffer); //从channel读取数据

            if (n <= 0) { //没有数据或者连接已关闭，推出循环
                break;
            }
            buffer.flip(); //反转buffer，切换成读模式
//            client.write(buffer); 将数据反写回channel
        }


    }
}
```

​	



## Netty

### Netty组建和设计

可以将Netty的组建分为三大类。

1. 处理和维护通道状态相关的组建——NIO demo中selector和channel相关的关系
   * Channel——Socket
   * EventLoop——控制流、多线程处理、并发
   * ChannelFuture——异步通知

2. 数据流和执行业务逻辑的相关组建——NIO demo中具体事件的处理方法

   * channelHandler

   * ChannelPipeline

3. 引导服务组建——启动Netty服务的引导类，服务器的配置参数，绑定的端口，使用的模型等各种。



Channel

EventLoop

ChannelFuture

ChannelHandler

ChannelPipeline

ServerBootStrap



#### 运行原理

![image-20201103190602304](./png/image-20201103190602304.png)



### Netty的简单demo

服务端代码

启动引导类

```java
public class EchoServer {
    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 1) {
            System.err.println(
                    "Usage: " +EchoServer.class.getSimpleName() + " <port>"
            );
            return;
        }
        int port = Integer.parseInt(args[0]);
        new EchoServer(port).start();
    }

    public void start() throws InterruptedException {
        final  EchoServerHandler serverHandler = new EchoServerHandler();
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(serverHandler);
                        }
                    });
            ChannelFuture f = b.bind().sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}
```



相关业务处理

```Java
@Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        System.out.println("Server received: " + in.toString(CharsetUtil.UTF_8));
        ctx.write(in);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
```



## 作业

基础代码可以 fork：[ https://github.com/kimmking/JavaCourseCodes](https://github.com/kimmking/JavaCourseCodes)
02nio/nio02 文件夹下
实现以后，代码提交到 Github。

**1.（必做）**整合你上次作业的 httpclient/okhttp；

**2.（选做）**使用 netty 实现后端 http 访问（代替上一步骤）

**3.（必做）**实现过滤器。
**4.（选做）**实现路由。



**1. 整合HttpClient**

思路：将InboundHandler中装配的handler，替换成自己实现的HttpClient。



```java
private HttpClientSender handler;

public HttpInboundHandler(String proxyServer) {
    this.proxyServer = proxyServer;
    handler = new HttpClientSender(this.proxyServer);
}
```



**2. 使用netty实现后端http访问**

思路：客服端请求   **→**     netty网关入站处理（发送到netty客户端）&rarr;  netty客户端入站处理（请求后端）&rarr;   后端处理返回 &rarr;  netty客户端出站处理（往网关通道写入后端数据）&rarr;   netty网关出站处理（往客户端通道写入netty客户端数据）&rarr;  客户端输出响应值

如果不对response响应的数据进行数据修改，那么可以不用单独实现OutboundHandler，直接将InboundHandler对于后端返回的数据写入通道，但这里需要注意编码问题。

这里使用装配模式，通过参数选择对应的Client，通过handle方法统一与后端服务进行通信。

![image-20201105150024726](./png/image-20201105150024726.png)



**3. 实现过滤器**

在请求流入InboundHandler的channelRead方法时，执行filter方法，添加一个FullHttpRequest的header。

```java
public class CustomFilter implements HttpRequestFilter {
    @Override
    public void filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        if (fullRequest != null) {
            fullRequest.headers().set("nio", "lijiajun");
        }
    }
}
```



```java
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) {
    FullHttpResponse response = null;
    FullHttpRequest fullRequest = (FullHttpRequest) msg;

    try {
        //过滤器
        CustomFilter customFilter = new CustomFilter();
        customFilter.filter(fullRequest, ctx);


        //调用任意装配的clientHandler 发送请求，netty阻塞结果
        String value = handler.handle(fullRequest, ctx);
```



**4. 实现路由**

这里的思路是将一组proxyServerList传给具体调用的Client，案列中实现了HttpClient和Netty，然后采用随机算法随机选用一台后端服务进行发送或连接。

```java
private String randomProxyServer() {
    Random random = new Random();
    int index = random.nextInt(proxyServerList.size());
    return proxyServerList.get(index);
}
```



**5. 性能压测**

分别对HttpClient和Netty模式下进行压力测试

wrk -c40 -d60S http://localhost:8888/api/hello



HttpClient(同步实现)：19161.59

Netty：1634.63 

测出来Netty性能较低，目测代码有问题。。。（继续改造。。。）

-------

引用：

1. http://ifeve.com/buffers/

2. https://www.tutorialspoint.com/java_nio/java_nio_quick_guide.htm

3. 《深入分析Java Web 技术内幕（修订版）》

4. 《Java NIO》

5. 《Netty 实战》
6. https://blog.csdn.net/weixin_30722589/article/details/96296914
