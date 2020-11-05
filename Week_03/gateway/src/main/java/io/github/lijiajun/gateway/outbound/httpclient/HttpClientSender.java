package io.github.lijiajun.gateway.outbound.httpclient;


import io.github.lijiajun.gateway.outbound.BaseClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Lijiajun
 * @date 2020/11/03 10:12
 */
public class HttpClientSender implements BaseClient {


    private HttpClient httpClient;

    private final List<String> proxyServerList;

    public HttpClientSender(List<String> proxyServerList) {
        this.proxyServerList = proxyServerList;
        httpClient = HttpConnectManager.getHttpClient();

    }

    private String randomProxyServer() {
        Random random = new Random();
        int index = random.nextInt(proxyServerList.size());
        return proxyServerList.get(index);
    }

    public String handle(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx) throws Exception {
        final String url = randomProxyServer() + fullRequest.uri();

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);

        return this.sendRequest(httpGet);
    }


    public String sendRequest(HttpRequestBase httpRequest) {
        String res;
        HttpResponse response;
        HttpEntity entity;
        try {
            response = httpClient.execute(httpRequest);
            entity = response.getEntity();

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                res = entity != null ? EntityUtils.toString(entity, "utf-8") : "";
                return res;
            }
            throw new RuntimeException("Http状态码错误");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("IO异常" + e.getMessage());
        } finally {
            httpRequest.releaseConnection();
        }
    }


    public static String HOST = "localhost";
    public static void main(String[] args) {
        List<String> serverList = new ArrayList<>();
        serverList.add("http://localhost:8088/api/hello");
        HttpClientSender httpClientDemo = new HttpClientSender(serverList);
        HttpGet httpGet = new HttpGet(HOST);
        try {
            String s = httpClientDemo.sendRequest(httpGet);
            System.out.println(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static class HttpConnectManager {
        private static PoolingHttpClientConnectionManager cm;
        /**
         * 最大连接数
         */
        public final static int MAX_TOTAL_CONNECTIONS = 500;

        /**
         * 每个路由最大连接数
         */
        public final static int MAX_ROUTE_CONNECTIONS = 300;
        static {
            cm = new PoolingHttpClientConnectionManager();
            cm.setMaxTotal(MAX_TOTAL_CONNECTIONS);

            cm.setDefaultMaxPerRoute(MAX_ROUTE_CONNECTIONS);
        }
        public static CloseableHttpClient getHttpClient() {
            //设置默认时间
            RequestConfig params = RequestConfig.custom().setConnectTimeout(10000).setConnectionRequestTimeout(10000).setSocketTimeout(10000)
                    .setExpectContinueEnabled(true).build();

            return HttpClients.custom()
                    .setConnectionManager(cm)
                    .setDefaultRequestConfig(params)
                    .build();
        }
    }


}
