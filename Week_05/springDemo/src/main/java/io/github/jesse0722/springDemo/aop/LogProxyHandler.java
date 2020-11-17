package io.github.jesse0722.springDemo.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Lijiajun
 * @date 2020/11/14 18:32
 */
public class LogProxyHandler implements InvocationHandler {

    /***
     * 需要代理的对象
     */
    private final Log targetObject;

    public LogProxyHandler(Log log) {
        this.targetObject = log;
    }

    /***
     * 代理方法
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        //Proxy Before...
        System.out.println("Proxy Before...");
        Object object =  method.invoke(targetObject, args);
        //Proxy After...
        System.out.println("Proxy After...");

        return object;
    }
}
