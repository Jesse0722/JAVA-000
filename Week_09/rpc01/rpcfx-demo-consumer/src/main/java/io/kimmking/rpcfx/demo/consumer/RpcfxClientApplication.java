package io.kimmking.rpcfx.demo.consumer;

import com.alibaba.fastjson.parser.ParserConfig;
import io.kimmking.rpcfx.api.Filter;
import io.kimmking.rpcfx.api.LoadBalancer;
import io.kimmking.rpcfx.api.Router;
import io.kimmking.rpcfx.api.RpcfxRequest;
import io.kimmking.rpcfx.client.Rpcfx;
import io.kimmking.rpcfx.client.aop.RpcService;
import io.kimmking.rpcfx.client.aop.RpcServiceAspect;
import io.kimmking.rpcfx.demo.api.Order;
import io.kimmking.rpcfx.demo.api.OrderService;
import io.kimmking.rpcfx.demo.api.User;
import io.kimmking.rpcfx.demo.api.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Random;

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

		UserService userService = Rpcfx.create(UserService.class, "localhost:8080");
		User user = userService.findById(1);
		System.out.println("find user id=1 from server: " + user.getName());

		OrderService orderService = Rpcfx.create(OrderService.class, "localhost:8080");
		Order order = orderService.findOrderById(1992129);
		System.out.println(String.format("find order name=%s, amount=%f",order.getName(),order.getAmount()));

		//从zk获取注册列表进行调用
		try {
			UserService fromRegistry = Rpcfx.createFromRegistry(UserService.class, "localhost:2181", new TagRouter(), new RandomLoadBalancer(), new SingleImplInterfaceFilter());
			System.out.println("zk:" + fromRegistry.findById(1));

		} catch (Exception e) {
			e.printStackTrace();
		}

		//SpringApplication.run(RpcfxClientApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		User user = userService.findById(11);
		System.out.println(user);
	}


	private static class TagRouter implements Router {
		@Override
		public List<String> route(List<String> urls) {
			return urls;
		}
	}

	/***
	 * 随机负载均衡器
	 */
	private static class RandomLoadBalancer implements LoadBalancer {
		@Override
		public String select(List<String> urls) {
			return urls.get(new Random().nextInt(urls.size()));
		}
	}

	@Slf4j
	private static class SingleImplInterfaceFilter implements Filter {
		@Override
		public boolean filter(RpcfxRequest request) {
			log.info("filter {} -> {}", this.getClass().getName(), request.toString());
			return true;

		}

		@Override
		public Filter next() {
			return this;
		}
	}
}
