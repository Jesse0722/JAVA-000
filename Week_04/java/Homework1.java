import java.util.concurrent.*;

/**
 * @author Lijiajun
 * @date 2020/11/11 16:52
 */

public class Homework1 {

    public static void main(String[] args) throws Exception{
        long start=System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Homework1 homework1 = new Homework1();


        // 异步执行 下面方法
        //1. 使用Executor提交一个callable任务
//        Future<Integer> future = executorService.submit(new Callable<Integer>() {
//            @Override
//            public Integer call() throws Exception {
//                return homework1.sum(20);
//            }
//        });
        //这是得到的返回值
//        int result = future.get();


        //2. 使用FutureTask
//        FutureTask<Integer> futureTask = new FutureTask<>(new Callable<Integer>() {
//            @Override
//            public Integer call() throws Exception {
//                return homework1.sum(20);
//            }
//        });
//        executorService.submit(futureTask);
//        int result = futureTask.get();

        //3. 使用completableFuture
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> homework1.sum(20));
        completableFuture.thenAccept( result -> System.out.println("异步计算结果为："+ result));

        // 确保  拿到result 并输出
//        System.out.println("异步计算结果为："+ result);

        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
    }

    public  int sum(int a) {
        return fibo(a);
    }

    private int fibo(int a) {
        if ( a < 2)
            return 1;
        return fibo(a-1) + fibo(a-2);
    }
}
