package io.github.lijiajun.gateway.inbound;


import io.github.lijiajun.gateway.outbound.BaseClient;
import io.github.lijiajun.gateway.outbound.ClientType;
import io.github.lijiajun.gateway.outbound.httpclient.HttpClientSender;
import io.github.lijiajun.gateway.outbound.netty4.NettyHttpClient;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.util.List;

public class HttpInboundInitializer extends ChannelInitializer<SocketChannel> {

	private BaseClient baseClient;

	public HttpInboundInitializer(List<String> proxyServerList, ClientType clientType) throws Exception {
		if (clientType == null || clientType.equals(ClientType.HttpClient) ) {
			this.baseClient = new HttpClientSender(proxyServerList);
		}
		else if (clientType.equals(ClientType.Netty)) {
			this.baseClient = new NettyHttpClient(proxyServerList);
		}

	}


	@Override
	public void initChannel(SocketChannel ch) {
		ChannelPipeline p = ch.pipeline();
//		if (sslCtx != null) {
//			p.addLast(sslCtx.newHandler(ch.alloc()));
//		}
		p.addLast(new HttpServerCodec());
		//p.addLast(new HttpServerExpectContinueHandler());
		p.addLast(new HttpObjectAggregator(1024 * 1024));
		p.addLast(new HttpInboundHandler(baseClient));
	}
}
