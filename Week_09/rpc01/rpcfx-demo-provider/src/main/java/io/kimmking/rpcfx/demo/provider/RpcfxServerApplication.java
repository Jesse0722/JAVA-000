package io.kimmking.rpcfx.demo.provider;

import io.kimmking.rpcfx.api.RpcfxRequest;
import io.kimmking.rpcfx.api.RpcfxResolver;
import io.kimmking.rpcfx.api.RpcfxResponse;
import io.kimmking.rpcfx.client.aop.RpcService;
import io.kimmking.rpcfx.demo.api.OrderService;
import io.kimmking.rpcfx.demo.api.UserService;
import io.kimmking.rpcfx.demo.provider.service.OrderServiceImpl;
import io.kimmking.rpcfx.demo.provider.service.UserServiceImpl;
import io.kimmking.rpcfx.server.RpcfxInvoker;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@SpringBootApplication
@RestController
public class RpcfxServerApplication {

	@Bean
	public CuratorFramework curatorFramework() {
		//连接zk
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		CuratorFramework client = CuratorFrameworkFactory.builder().connectString("localhost:2181").namespace("rpc-demo").retryPolicy(retryPolicy).build();
		client.start();
		return client;
	}

	public static void main(String[] args) {
		SpringApplication.run(RpcfxServerApplication.class, args);

	}

	@Autowired
	RpcfxInvoker invoker;

	@GetMapping("/test")
	public String invoke() {
		return "Success!";
	}

	@PostMapping("/")
	public RpcfxResponse invoke(@RequestBody RpcfxRequest request) {
		return invoker.invoke(request);
	}

	@Bean
	public RpcfxInvoker createInvoker(@Autowired RpcServiceRegister register){
		return new RpcfxInvoker(register.getServiceContext());
	}

	@Bean
	public RpcfxResolver createResolver(){
		return new DemoResolver();
	}

	// 能否去掉name
	// 注解
	@Bean
	public UserService createUserService(){
		System.out.println("createUserService");
		return new UserServiceImpl();
	}

	@Bean
	public OrderService createOrderService(){
		return new OrderServiceImpl();
	}


	@Bean
	public RpcServiceRegister rpcServiceRegister() {
		return new RpcServiceRegister("io.kimmking.rpcfx.demo.provider.service");
	}
}
