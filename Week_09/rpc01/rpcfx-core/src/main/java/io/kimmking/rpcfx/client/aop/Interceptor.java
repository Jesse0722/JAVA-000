package io.kimmking.rpcfx.client.aop;

import com.alibaba.fastjson.JSON;
import io.kimmking.rpcfx.api.Filter;
import io.kimmking.rpcfx.api.RpcfxRequest;
import io.kimmking.rpcfx.api.RpcfxResponse;
import io.kimmking.rpcfx.client.netty4.NettyHttpClient;
import net.bytebuddy.implementation.bind.annotation.*;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import sun.misc.URLClassPath;
import sun.net.util.URLUtil;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * @author Lijiajun
 * @date 2020/12/16 15:00
 */
public class Interceptor {

    public static final MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");

    private String url;
    private Filter[] filters;

    public Interceptor(String url, Filter[] filters) {
        this.url = url;
        this.filters = filters;
    }

    @RuntimeType
    public Object intercept(@Origin Method method, @AllArguments Object[] args) throws Throwable {
        RpcfxRequest rpcfxRequest = new RpcfxRequest();
        rpcfxRequest.setMethod(method.getName());
        rpcfxRequest.setServiceClass(method.getDeclaringClass().getName());
        rpcfxRequest.setParams(args);

        if (null!=filters) {
            for (Filter filter : filters) {
                if (!filter.filter(rpcfxRequest)) {
                    return null;
                }
            }
        }
        // 1.可以复用client
        // 2.尝试使用httpclient或者netty client
        String[] s = url.split(":");
        NettyHttpClient httpClient = new NettyHttpClient(s[0], Integer.parseInt(s[1]));

        RpcfxResponse response = httpClient.send(rpcfxRequest);
        return JSON.parse(response.getResult().toString());
    }

}
