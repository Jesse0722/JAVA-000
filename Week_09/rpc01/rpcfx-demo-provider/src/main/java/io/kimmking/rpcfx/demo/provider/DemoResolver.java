package io.kimmking.rpcfx.demo.provider;

import io.kimmking.rpcfx.api.RpcfxResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DemoResolver implements RpcfxResolver, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    /***
     * 这个方法的作用是获取serviceClass真正的实现类
     * @param serviceClass
     * @return
     */
    @Override
    public Object resolve(String serviceClass) {
        return this.applicationContext.getBean(serviceClass);
        // 从bean容器中获取实现类。 采用反射加泛型如何替换？ 根据接口名称-》反射出改接口-》获取该接口的所有实现类-》找到匹配的方法-》invoke方法-》返回值
    }
}
