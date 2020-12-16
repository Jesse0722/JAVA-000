package io.kimmking.rpcfx.client.aop;

import com.alibaba.fastjson.JSON;
import io.kimmking.rpcfx.api.RpcfxRequest;
import io.kimmking.rpcfx.api.RpcfxResponse;
import net.bytebuddy.implementation.bind.annotation.*;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * @author Lijiajun
 * @date 2020/12/16 15:00
 */
public class Interceptor {

    public static final MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");

    @RuntimeType
    public Object intercept(@Origin Method method, @AllArguments Object[] args) throws Throwable {
        RpcfxRequest rpcfxRequest = new RpcfxRequest();
        rpcfxRequest.setMethod(method.getName());
        rpcfxRequest.setServiceClass(method.getDeclaringClass().getName());
        rpcfxRequest.setParams(args);


        String reqJson = JSON.toJSONString(rpcfxRequest);
        System.out.println("req json: "+reqJson);

        // 1.可以复用client
        // 2.尝试使用httpclient或者netty client
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url("http://localhost:8080/")
                .post(RequestBody.create(JSONTYPE, reqJson))
                .build();
        String respJson = client.newCall(request).execute().body().string();
        System.out.println("resp json: "+respJson);
        RpcfxResponse response = JSON.parseObject(respJson, RpcfxResponse.class);
        return JSON.parse(response.getResult().toString());
    }

}
