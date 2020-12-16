package io.kimmking.rpcfx.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.kimmking.rpcfx.api.RpcfxRequest;
import io.kimmking.rpcfx.api.RpcfxResolver;
import io.kimmking.rpcfx.api.RpcfxResponse;
import io.kimmking.rpcfx.exception.RpcfxException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

public class RpcfxInvoker {

    private RpcfxResolver resolver;

    private HashMap<String, Class<?>> serviceContext;

    public RpcfxInvoker(RpcfxResolver resolver){
        this.resolver = resolver;
    }

    public RpcfxInvoker(HashMap<String, Class<?>> serviceContext) {
        this.serviceContext = serviceContext;
    }

    public RpcfxResponse invoke(RpcfxRequest request) {
        RpcfxResponse response = new RpcfxResponse();
        String serviceClass = request.getServiceClass();

        // 作业1：改成泛型和反射
//        Object service = resolver.resolve(serviceClass);//this.applicationContext.getBean(serviceClass);
        // 更换通过spring容器查找实现类的方式，采用自己的注册表进行查找
        Class<?> implService = this.serviceContext.get(serviceClass);

        try {
            Method method = resolveMethodFromClass(implService, request.getMethod());
            Object result = method.invoke(implService.newInstance(), request.getParams()); // dubbo, fastjson,
            // 两次json序列化能否合并成一个
            response.setResult(JSON.toJSONString(result, SerializerFeature.WriteClassName));
            response.setStatus(true);
            return response;
        } catch ( IllegalAccessException | InvocationTargetException | InstantiationException e ) {
            e.printStackTrace();

            // 3.Xstream

            // 2.封装一个统一的RpcfxException
            // 客户端也需要判断异常
            RpcfxException rpcfxException = new RpcfxException(e.getMessage());
            response.setException(rpcfxException);
            response.setStatus(false);
            return response;
        }
    }

    private Method resolveMethodFromClass(Class<?> klass, String methodName) {
        return Arrays.stream(klass.getMethods()).filter(m -> methodName.equals(m.getName())).findFirst().get();
    }

}
