package io.kimmking.rpcfx.exception;

/**
 * @author Lijiajun
 * @date 2020/12/14 18:37
 */
public class RpcfxException extends RuntimeException {
    public RpcfxException(String message) {
        super(message);
    }

    public RpcfxException(String message, Throwable cause) {
        super(message, cause);
    }
}
