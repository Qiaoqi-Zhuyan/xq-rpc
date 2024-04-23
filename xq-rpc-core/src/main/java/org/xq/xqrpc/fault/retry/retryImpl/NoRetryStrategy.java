package org.xq.xqrpc.fault.retry.retryImpl;

import org.xq.xqrpc.fault.retry.RetryStrategy;
import org.xq.xqrpc.model.RpcResponse;

import java.util.concurrent.Callable;

public class NoRetryStrategy implements RetryStrategy {

    /**
     * 不进行重试
     * @param callable
     * @return
     * @throws Exception
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }
}
