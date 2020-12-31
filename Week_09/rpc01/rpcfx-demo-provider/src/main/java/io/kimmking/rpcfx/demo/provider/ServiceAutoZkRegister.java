package io.kimmking.rpcfx.demo.provider;

import io.kimmking.rpcfx.api.ServiceProviderDesc;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lijiajun
 * @date 2020/12/29 3:31 PM
 */
@Component
public class ServiceAutoZkRegister implements ApplicationContextAware, SmartInitializingSingleton {

    private CuratorFramework client;

    private ApplicationContext applicationContext;

    @Autowired
    public ServiceAutoZkRegister(CuratorFramework client) {
        this.client = client;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        this.applicationContext = applicationContext;
    }

    private void registerService(CuratorFramework client, String service) throws Exception {
        ServiceProviderDesc userServiceSesc = ServiceProviderDesc.builder()
                .host(InetAddress.getLocalHost().getHostAddress())
                .port(8080).serviceClass(service).build();

        try {
            if ( null == client.checkExists().forPath("/" + service)) {
                client.create().withMode(CreateMode.PERSISTENT).forPath("/" + service, "service".getBytes());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //临时节点
        client.create().withMode(CreateMode.EPHEMERAL).
                forPath( "/" + service + "/" + userServiceSesc.getHost() + ":" + userServiceSesc.getPort(), "provider".getBytes());
    }

    @Override
    public void afterSingletonsInstantiated() {
        if(applicationContext != null) {
            //        System.out.println("setApplicationContext");
            Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(RpcImplService.class);

            //自动注册到zk
            beansWithAnnotation.values().forEach( value -> {
                Class<?>[] interfaces = value.getClass().getInterfaces();
                if (interfaces.length != 1) {
                    throw new RuntimeException("Service Auto Register Error!" + value.getClass().getName() + "can only implement one interface.") ;
                }
                String serviceKey = interfaces[0].getName();
                try {
                    registerService(client, serviceKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
