package io.github.lijiajun.gateway.outbound.netty4;

import io.github.lijiajun.gateway.outbound.BaseClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class NettyHttpClient implements BaseClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyHttpClient.class);


    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    private Bootstrap b = new Bootstrap();

    /**
     * 客户端业务处理handler
     */
    private NettyHttpClientInboundHandler clientHandler = new NettyHttpClientInboundHandler();


    /**
     * 客户端通道
     */
    private Channel clientChannel;

    private String backendUrl;

    private final List<String> proxyServerList;


    private InetSocketAddress randomProxyServer() {
        Random random = new Random();
        int index = random.nextInt(proxyServerList.size());
        this.backendUrl = proxyServerList.get(index);
        URI uri = null;
        try {
            uri = new URI(backendUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return new InetSocketAddress(uri.getHost(), uri.getPort());
    }

    public NettyHttpClient(List<String> proxyServerList) throws Exception {
        this.proxyServerList = proxyServerList;
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                // 客户端接收到的是httpResponse响应，所以要使用HttpResponseDecoder进行解码
                ch.pipeline().addLast(new HttpResponseDecoder());
                //客户端发送的是httprequest，所以要使用HttpRequestEncoder进行编码
                ch.pipeline().addLast(new HttpRequestEncoder());
                ch.pipeline().addLast(clientHandler);
            }
        });

        // Start the client.
        ChannelFuture channelFuture = b.connect(randomProxyServer()).sync();
        System.out.println("connected...");

        //注册连接事件
        channelFuture.addListener((ChannelFutureListener)future -> {
            //如果连接成功
            if (future.isSuccess()) {
                logger.info("客户端[" + channelFuture.channel().localAddress().toString() + "]已连接...");
                clientChannel = channelFuture.channel();
            }
            //如果连接失败，尝试重新连接
            else{
                logger.info("客户端[" + channelFuture.channel().localAddress().toString() + "]连接失败，重新连接中...");
                future.channel().close();
                b.connect(randomProxyServer());
            }
        });

        //注册关闭事件
        channelFuture.channel().closeFuture().addListener(cfl -> {
            close();
            logger.info("客户端[" + channelFuture.channel().localAddress().toString() + "]已断开...");
        });
    }

    /**
     * 客户端关闭
     */
    private void close() {
        //关闭客户端套接字
        if(clientChannel!=null){
            clientChannel.close();
        }
        //关闭客户端线程组
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    /***
     * 阻塞3秒，同步到执行结果，否则中断异常
     * @param message
     * @return
     * @throws InterruptedException
     */
    public String send(FullHttpRequest message) throws InterruptedException {
        ChannelPromise promise = clientHandler.sendMessage(message);
        promise.await(3, TimeUnit.SECONDS);
        return clientHandler.getResponse();
    }

    @Override
    public String handle(FullHttpRequest fullRequest, ChannelHandlerContext ctx) throws Exception {
        final String url = this.backendUrl + fullRequest.uri();

        URI uri = new URI(url);
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,HttpMethod.GET,
                uri.toASCIIString(), Unpooled.wrappedBuffer(fullRequest.content()));
        request.headers().set(HttpHeaders.Names.HOST, "127.0.0.1");
        request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, request.content().readableBytes());

        return send(fullRequest);

    }

//    public static void main(String[] args) throws Exception {
//        NettyHttpClient client = new NettyHttpClient("127.0.0.1", 8088);
//
//        URI uri = new URI("http://localhost:8088/api/hello");
//        String msg = "Are you Ok？";
//        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,HttpMethod.GET,
//                uri.toASCIIString(), Unpooled.wrappedBuffer(msg.getBytes("UTF-8")));
//        request.headers().set(HttpHeaders.Names.HOST, "127.0.0.1");
//        request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
//        request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, request.content().readableBytes());
//
//        String r = client.send(request);
//        System.out.println(r);
//
//        client.close();
//    }
}
