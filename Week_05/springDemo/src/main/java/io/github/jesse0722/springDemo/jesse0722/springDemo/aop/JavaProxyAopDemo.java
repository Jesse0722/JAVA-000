package io.github.jesse0722.springDemo.jesse0722.springDemo.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author Lijiajun
 * @date 2020/11/14 17:53
 */
public class JavaProxyAopDemo {
    public static void main(String[] args) {
        //定义代理处理器用来执行代理类的方法调用
        InvocationHandler handler = new LogProxyHandler(new Logger());
        IHello o = (IHello) Proxy.newProxyInstance(handler.getClass().getClassLoader(), new Class[]{Log.class, IHello.class}, handler);
        System.out.println(Proxy.isProxyClass(o.getClass()));
        o.hello();
    }
}
