package primePaySdk;

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
import org.apache.http.util.EntityUtils;

import java.io.IOException;


/**
 * @author Lijiajun
 * @date 2020/10/28 10:51
 */
public class HttpClientDemo {

    private HttpClient httpClient;

    public HttpClientDemo() {
        httpClient = HttpConnectManager.getHttpClient();
    }

    private String sendRequest(HttpRequestBase httpRequest) {
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


    public static String HOST = "http://localhost:8801";
    public static void main(String[] args) {
        HttpClientDemo httpClientDemo = new HttpClientDemo();
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
