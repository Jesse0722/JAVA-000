## 作业

1. 思考有多少种方式，在 main 函数启动一个新线程，运行一个方法，拿到这个方法的返回值后，退出主线程？

   代码见./java/Homework1.java
   思路：Runnale接口run方法是没有返回值的，Callable接口有返回值，我们可以利用Executor执行器来提交callable任务。

   1） 使用Future来接受返回值

   ```java
   ExecutorService executorService = Executors.newFixedThreadPool(2);
   Future<Integer> future = executorService.submit(new Callable<Integer>() {
     @Override
     public Integer call() throws Exception {
       return homework1.sum(20);
     }
   });
   //同步接受返回值，主线程将阻塞。
   System.out.println("异步计算结果为："+ future.get());
   ```

   2) 使用FutureTask，本质还是使用了Future来接受返回值

```java
FutureTask<Integer> futureTask = new FutureTask<>(new Callable<Integer>() {
    @Override
    public Integer call() throws Exception {
        return homework1.sum(20);
    }
});
executorService.submit(futureTask);
int result = futureTask.get();
```

​		3）使用CompletableFuture，CompletableFuture可以支持函数式的调用，异步执行一个任务，当结果返回时自动执行一个方法，好处是不用阻塞主线程。

```java
CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> homework1.sum(20));
completableFuture.thenAccept( result -> System.out.println("异步计算结果为："+ result));
```

