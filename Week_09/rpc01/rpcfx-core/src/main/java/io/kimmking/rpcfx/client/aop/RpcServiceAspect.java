package io.kimmking.rpcfx.client.aop;

import com.alibaba.fastjson.JSON;
import io.kimmking.rpcfx.api.RpcfxRequest;
import io.kimmking.rpcfx.api.RpcfxResponse;
import io.kimmking.rpcfx.client.netty4.NettyHttpClient;
import io.kimmking.rpcfx.exception.RpcfxException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 定义切面，切点为标记@RpcSerivice注解的方法，环绕型通知
 * @author Lijiajun
 * @date 2020/12/14 17:21
 */
@Aspect
public class RpcServiceAspect {
    private static final MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");


    @Around("@annotation(io.kimmking.rpcfx.client.aop.RpcService)")
    public Object around(ProceedingJoinPoint point) {
        String methodName = point.getSignature().getName();
        Object[] methodArgs = point.getArgs();
        RpcfxRequest request = new RpcfxRequest();
        request.setMethod(methodName);
        request.setParams(methodArgs);
        request.setServiceClass(point.getTarget().getClass().getInterfaces()[0].getName());

        RpcfxResponse response;
        try {
            response = post(request);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RpcfxException(e.getMessage());
        }

        return JSON.parse(response.getResult().toString());

    }

    private RpcfxResponse post(RpcfxRequest req) throws IOException {
//        String reqJson = JSON.toJSONString(req);
//        System.out.println("req json: "+reqJson);
//
//        // 1.可以复用client
//        // 2.尝试使用httpclient或者netty client
//        OkHttpClient client = new OkHttpClient();
//        final Request request = new Request.Builder()
//                .url(url)
//                .post(RequestBody.create(JSONTYPE, reqJson))
//                .build();
//        String respJson = client.newCall(request).execute().body().string();
//        System.out.println("resp json: "+respJson);
//        return JSON.parseObject(respJson, RpcfxResponse.class);

        try {
            NettyHttpClient nettyHttpClient = new NettyHttpClient("127.0.0.1", 8080);
            return nettyHttpClient.send(req);

        } catch (Exception e) {
            e.printStackTrace();
            RpcfxResponse response = new RpcfxResponse();
            response.setStatus(false);
            response.setException(new RpcfxException(e.getMessage()));
            response.setResult(null);
            return response;
        }
    }
}
