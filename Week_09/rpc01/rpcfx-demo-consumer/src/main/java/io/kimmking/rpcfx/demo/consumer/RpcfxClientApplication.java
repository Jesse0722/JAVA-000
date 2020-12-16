package io.kimmking.rpcfx.demo.consumer;

import com.alibaba.fastjson.parser.ParserConfig;
import io.kimmking.rpcfx.client.Rpcfx;
import io.kimmking.rpcfx.client.aop.RpcService;
import io.kimmking.rpcfx.client.aop.RpcServiceAspect;
import io.kimmking.rpcfx.demo.api.Order;
import io.kimmking.rpcfx.demo.api.OrderService;
import io.kimmking.rpcfx.demo.api.User;
import io.kimmking.rpcfx.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RpcfxClientApplication implements CommandLineRunner {

	static {
		ParserConfig.getGlobalInstance().addAccept("io.kimmking");
	}
	// 二方库
	// 三方库 lib
	// nexus, userserivce -> userdao -> user
	//
	@Bean
	public RpcServiceAspect rpcService() {
		return new RpcServiceAspect();
	}


	@Autowired
	private UserService userService;

	public static void main(String[] args) throws InstantiationException, IllegalAccessException {

		// UserService service = new xxx();
		// service.findById

		UserService userService = Rpcfx.create(UserService.class, "http://localhost:8080/");
		User user = userService.findById(1);
		System.out.println("find user id=1 from server: " + user.getName());

		OrderService orderService = Rpcfx.create(OrderService.class, "http://localhost:8080/");
		Order order = orderService.findOrderById(1992129);
		System.out.println(String.format("find order name=%s, amount=%f",order.getName(),order.getAmount()));

		// 新加一个OrderService

		//SpringApplication.run(RpcfxClientApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		User user = userService.findById(11);
		System.out.println(user);
	}
}
