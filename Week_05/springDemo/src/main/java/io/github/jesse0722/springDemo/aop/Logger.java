package io.github.jesse0722.springDemo.aop;

/**
 * @author Lijiajun
 * @date 2020/11/14 19:10
 */
public class Logger implements Log, IHello {
    @Override
    public void log(String str) {
        System.out.println("Thread[" + Thread.currentThread().getName() + "]-Class[" + this.getClass().getName()
        +"]-Method[" + Thread.currentThread().getStackTrace()[1].getMethodName() + "]-LineNo[" +
                Thread.currentThread().getStackTrace()[1].getLineNumber() + "]  Log:" + str);
    }

    @Override
    public void log(String par1, Integer par2) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName()  + " Par1：" + par1 + " Par2:" + par2);
    }

    @Override
    public void log2(String par1, Integer par2) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName() + " Par1：" + par1 + " Par2:" + par2);
    }

    @Override
    public void hello() {
        System.out.println("Hello world!!!!!");
    }
}
