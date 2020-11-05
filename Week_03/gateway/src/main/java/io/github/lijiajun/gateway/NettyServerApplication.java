package io.github.lijiajun.gateway;



import io.github.lijiajun.gateway.inbound.HttpInboundServer;
import io.github.lijiajun.gateway.outbound.ClientType;

import java.util.ArrayList;
import java.util.List;

public class NettyServerApplication {

    public final static String GATEWAY_NAME = "NIOGateway";
    public final static String GATEWAY_VERSION = "1.0.0";

    public static void main(String[] args) {
        String proxyServer = System.getProperty("proxyServer","localhost");
        String proxyPort = System.getProperty("proxyPort","8088");
        String port = System.getProperty("port","8888");

          //  http://localhost:8888/api/hello  ==> gateway API
          //  http://localhost:8088/api/hello  ==> backend service

        List<String> proxyServerList = new ArrayList<>();
        proxyServerList.add("http://localhost:8088");
        proxyServerList.add("http://localhost:8088");


        System.out.println(GATEWAY_NAME + " " + GATEWAY_VERSION +" starting...");
        HttpInboundServer server = new HttpInboundServer(Integer.parseInt(port), proxyServerList, ClientType.Netty);
        System.out.println(GATEWAY_NAME + " " + GATEWAY_VERSION +" started at http://localhost:" + port + " for server:" + proxyServer);
        try {
            server.run();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
