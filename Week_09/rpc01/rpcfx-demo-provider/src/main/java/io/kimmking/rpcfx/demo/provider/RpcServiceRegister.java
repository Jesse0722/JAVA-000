package io.kimmking.rpcfx.demo.provider;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author Lijiajun
 * @date 2020/12/15 10:27
 */
public class RpcServiceRegister {

    public HashMap<String, Class<?>> serviceContext = new HashMap<>();


    /***
     * 获取所有service目录下的实现类
     * @param servicePath
     */
    public RpcServiceRegister(String servicePath) {
        Set<Class<?>> implServiceList = ClassUtil.getClasses(servicePath);

        for (Class clz : implServiceList) {
            Class[] interfaces = clz.getInterfaces();
            for (Class inf: interfaces) {
                if (clz.getSimpleName().startsWith(inf.getSimpleName())) {
                    serviceContext.put(inf.getName(), clz);
                }
            }
        }
    }

    public HashMap<String, Class<?>> getServiceContext() {
        return serviceContext;
    }
}
