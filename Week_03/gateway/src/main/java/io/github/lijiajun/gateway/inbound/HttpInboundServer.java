package io.github.lijiajun.gateway.inbound;

import io.github.lijiajun.gateway.outbound.ClientType;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class HttpInboundServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpInboundServer.class);

    private final int port;

    private final List<String>  proxyServerList;

    private ClientType clientType;
    /***
     *
     * @param port 服务端绑定的端口
     * @param proxyServerList 后端服务列表
     */
    public HttpInboundServer(final int port, final List<String> proxyServerList, ClientType clientType) {
        if (proxyServerList.isEmpty()) {
            throw new RuntimeException("ProxyServerList is empty.");
        }
        this.port=port;
        this.proxyServerList = proxyServerList;
        this.clientType = clientType;
    }

    public void run() throws Exception {
        //boss负责处理server端连接
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        //worker负责处理client端连接
        EventLoopGroup workerGroup = new NioEventLoopGroup(16);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                    .option(ChannelOption.SO_SNDBUF, 32 * 1024)
                    .option(EpollChannelOption.SO_REUSEPORT, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

            //1.bossGroup是workerGroup的parent，绑定NIO类型通道
            //绑定一个handler 通过添加handler，我们可以监听Channel的各种动作以及状态的改变，包括连接，绑定，接收消息
            //childHandler，它的目的是添加handler，用来监听已经连接的客户端的Channel的动作和状态
            // handler在初始化时就会执行，而childHandler会在客户端成功connect后才执行，这是两者的区别。
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO)).childHandler(new HttpInboundInitializer(proxyServerList, clientType));

            //bind()异步地绑定服务器，调用分sync()方法阻塞等待知道绑定完成，
            Channel ch = b.bind(port).sync().channel();
            logger.info("开启netty http服务器，监听地址和端口为 http://127.0.0.1:" + port + '/');
            System.out.println("开启netty http服务器，监听地址和端口为 http://127.0.0.1:" + port + '/');

            //获取channel的CloseFuture，并且阻塞当前线程知道它完成
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
