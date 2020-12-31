package io.kimmking.rpcfx.client;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import io.kimmking.rpcfx.api.*;
import io.kimmking.rpcfx.client.aop.Interceptor;
import io.kimmking.rpcfx.client.aop.RpcService;
import io.kimmking.rpcfx.client.aop.Test;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.matcher.ElementMatchers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.util.Assert;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.function.Function;

public final class Rpcfx {

    static {
        ParserConfig.getGlobalInstance().addAccept("io.kimmking");
    }

    public static <T, filters> T createFromRegistry(final Class<T> serviceClass, final String zkUrl, Router router, LoadBalancer loadBalance, Filter filter) throws Exception {

        // 加filter之一
        // start zk client
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework newClient = CuratorFrameworkFactory.builder().connectString(zkUrl).namespace("rpc-demo").retryPolicy(retryPolicy).build();
        newClient.start();
        //获取需要获取服务下对应的机器节点
        List<String> rpcfx = newClient.getChildren().forPath("/" + serviceClass.getName());
        // curator Provider list from zk
        List<String> invokers = rpcfx;
        // 1. 简单：从zk拿到服务提供的列表
        // 2. 挑战：监听zk的临时节点，根据事件更新这个list（注意，需要做个全局map保持每个服务的提供者List）

        List<String> urls = router.route(invokers);

        String url = loadBalance.select(urls); // router, loadbalance
        return (T) create(serviceClass, url, filter);

    }



    public static <T> T create(final Class<T> serviceClass, final String url, Filter... filters) throws IllegalAccessException, InstantiationException {

        // 0. 替换动态代理 -> 字节码生成

        //return (T) Proxy.newProxyInstance(Rpcfx.class.getClassLoader(), new Class[]{serviceClass}, new RpcfxInvocationHandler(serviceClass, url));
        return new ByteBuddy()
                .subclass(serviceClass)
                .method(ElementMatchers.any())
                .intercept(MethodDelegation.to(new Interceptor(url,filters)))
                .make()
                .load(Rpcfx.class.getClassLoader())
                .getLoaded().newInstance();

    }

    public static class RpcfxInvocationHandler implements InvocationHandler {

        public static final MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");

        private final Class<?> serviceClass;
        private final String url;
        public <T> RpcfxInvocationHandler(Class<T> serviceClass, String url) {
            this.serviceClass = serviceClass;
            this.url = url;
        }

        // 可以尝试，自己去写对象序列化，二进制还是文本的，，，rpcfx是xml自定义序列化、反序列化，json: code.google.com/p/rpcfx
        // int byte char float double long bool
        // [], data class

        @Override
        public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
            RpcfxRequest request = new RpcfxRequest();
            request.setServiceClass(this.serviceClass.getName());
            request.setMethod(method.getName());
            request.setParams(params);

            RpcfxResponse response = post(request, url);

            // 这里判断response.status，处理异常
            // 考虑封装一个全局的RpcfxException

            return JSON.parse(response.getResult().toString());
        }

        private RpcfxResponse post(RpcfxRequest req, String url) throws IOException {
            String reqJson = JSON.toJSONString(req);
            System.out.println("req json: "+reqJson);

            // 1.可以复用client
            // 2.尝试使用httpclient或者netty client
            OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(JSONTYPE, reqJson))
                    .build();
            String respJson = client.newCall(request).execute().body().string();
            System.out.println("resp json: "+respJson);
            return JSON.parseObject(respJson, RpcfxResponse.class);
        }
    }
    public void byteBuddyTest() throws IllegalAccessException, InstantiationException {
        Class<?> dynamicType = new ByteBuddy()
                .subclass(Function.class)
                .method(ElementMatchers.named("apply"))
                .intercept(FixedValue.value("Hello World!"))
                .make()
                .load(getClass().getClassLoader())
                .getLoaded();

        Assert.isTrue(dynamicType.newInstance().toString().equals("Hello World!"), "断言异常");
    }

    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        Rpcfx rpcfx = new Rpcfx();
        rpcfx.byteBuddyTest();
    }
}
