package io.kimmking.rpcfx.client.aop;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.aop.support.MethodMatchers;

/**
 * @author Lijiajun
 * @date 2020/12/16 11:27
 */
public class ByteBuddyTest {

    static class Foo {
        public static String sayHelloFoo() {
            return "Hello in Foo!";
        }
    }

    static class Bar {
        public static String sayHelloBar() {
            return "Holla in Bar!";
        }
    }





    public void test() throws IllegalAccessException, InstantiationException {
//        DynamicType.Unloaded<Test> dynamicType = new ByteBuddy()
//                .subclass(Test.class)
//                .method(ElementMatchers.named("greet"))
//                .intercept(MethodDelegation.to(new GreetingInterceptor()))
////                .name("example.Type")  //命名类型这里会以example.FooByteBuddy1376491271 后面随机数字，如果没有命名则会随机
//                .make();
////                .load(getClass().getClassLoader())
////                .getLoaded();
//
//        dynamicType.load(getClass().getClassLoader()).getLoaded().newInstance().greet();
        String sayHelloFoo = new ByteBuddy()
                .subclass(Test.class)
                .method(ElementMatchers.any())
                .intercept(MethodDelegation.to(new Interceptor()))
                .make()
                .load(getClass().getClassLoader())
                .getLoaded().newInstance().hello();
        System.out.println(sayHelloFoo);
    }
    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        ByteBuddyTest byteBuddyTest = new ByteBuddyTest();
        byteBuddyTest.test();
    }
}
