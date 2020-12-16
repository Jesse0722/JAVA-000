package io.kimmking.rpcfx.client.aop;

import java.lang.annotation.*;

/**
 * @author Lijiajun
 * @date 2020/12/14 17:18
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcService {
}
