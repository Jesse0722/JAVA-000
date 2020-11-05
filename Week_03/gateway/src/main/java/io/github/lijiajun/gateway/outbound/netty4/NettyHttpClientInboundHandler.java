package io.github.lijiajun.gateway.outbound.netty4;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class NettyHttpClientInboundHandler extends ChannelInboundHandlerAdapter{


    private static final Logger logger = LoggerFactory.getLogger(NettyHttpClientInboundHandler.class);

    private ChannelHandlerContext ctx;

    private ChannelPromise promise;

    private String response;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel actived");
        System.out.println(ctx.channel().remoteAddress());
        super.channelActive(ctx);

        this.ctx = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("=========" + ctx.name());

        if (msg instanceof HttpResponse)
        {
            HttpResponse response = (HttpResponse) msg;
            System.out.println("CONTENT_TYPE:" + response.headers().get(HttpHeaders.Names.CONTENT_TYPE));
        }
        if(msg instanceof HttpContent)
        {
            HttpContent content = (HttpContent)msg;
            ByteBuf buf = content.content();


//            System.out.println(buf.toString(io.netty.util.CharsetUtil.UTF_8));
            this.response = buf.toString(io.netty.util.CharsetUtil.UTF_8);
            promise.setSuccess();
            ctx.write(this.response);
            buf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    public synchronized ChannelPromise sendMessage(FullHttpRequest message) {
        while (ctx == null) {
            try {
                TimeUnit.MILLISECONDS.sleep(1);
                //logger.error("等待ChannelHandlerContext实例化");
            } catch (InterruptedException e) {
                logger.error("等待ChannelHandlerContext实例化过程中出错",e);
            }
        }
        promise = ctx.newPromise();
        ctx.writeAndFlush(message);
//        ctx.channel().writeAndFlush(message);

        return promise;
    }

    public String getResponse(){
        return this.response;
    }

}
