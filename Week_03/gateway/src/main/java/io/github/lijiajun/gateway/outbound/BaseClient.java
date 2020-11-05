package io.github.lijiajun.gateway.outbound;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author Lijiajun
 * @date 2020/11/04 20:53
 */
public interface BaseClient {
    String handle(FullHttpRequest fullRequest, ChannelHandlerContext ctx) throws Exception;

}
