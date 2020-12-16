package io.kimmking.rpcfx.demo.consumer;

import io.kimmking.rpcfx.client.aop.RpcService;
import io.kimmking.rpcfx.demo.api.User;
import io.kimmking.rpcfx.demo.api.UserService;
import org.springframework.stereotype.Component;

/**
 * @author Lijiajun
 * @date 2020/12/14 21:37
 */
@Component
public class UserServiceImpl implements UserService {

    @RpcService
    @Override
    public User findById(int id) {
        return null;
    }
}
