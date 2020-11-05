package io.github.lijiajun.gateway.filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author Lijiajun
 * @date 2020/11/05 12:54
 */
public class CustomFilter implements HttpRequestFilter {
    @Override
    public void filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        if (fullRequest != null) {
            fullRequest.headers().set("nio", "lijiajun");
        }
    }
}
