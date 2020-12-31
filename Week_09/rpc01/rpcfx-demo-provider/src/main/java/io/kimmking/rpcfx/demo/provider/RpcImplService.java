package io.kimmking.rpcfx.demo.provider;

import java.lang.annotation.*;

/**
 * @author Lijiajun
 * @date 2020/12/29 3:52 PM
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcImplService {
}
