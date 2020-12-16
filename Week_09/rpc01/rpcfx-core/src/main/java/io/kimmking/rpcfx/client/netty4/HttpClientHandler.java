package io.kimmking.rpcfx.client.netty4;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.kimmking.rpcfx.api.RpcfxRequest;
import io.kimmking.rpcfx.api.RpcfxResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

/**
 * @author Lijiajun
 * @date 2020/12/15 15:19
 */
public class HttpClientHandler extends ChannelInboundHandlerAdapter {
    private ChannelHandlerContext ctx;
    private ChannelPromise promise;
    private volatile RpcfxResponse rpcfxResponse;

    private CountDownLatch countDownLatch;

    public void setLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    public ChannelPromise flushMessage(FullHttpRequest request) {
        if (ctx == null)
            throw new IllegalStateException();

        promise = ctx.writeAndFlush(request).channel().newPromise();
        return promise;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.ctx = ctx;
        System.out.println("已连接");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("msg -> "+msg);

        System.out.println(ctx == this.ctx);
        if(msg instanceof FullHttpResponse){
            FullHttpResponse response = (FullHttpResponse)msg;
            ByteBuf buf = response.content();
            String result = buf.toString(CharsetUtil.UTF_8);
            this.rpcfxResponse = JSON.parseObject(result, RpcfxResponse.class);
            countDownLatch.countDown();
        }
    }

    public RpcfxResponse getRpcfxResponse() throws InterruptedException {
        countDownLatch.await();
        return this.rpcfxResponse;
    }

}
