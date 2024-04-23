package org.xq.xqrpc.fault.retry.retryImpl;

import com.github.rholder.retry.*;
import lombok.extern.slf4j.Slf4j;
import org.xq.xqrpc.fault.retry.RetryStrategy;
import org.xq.xqrpc.model.RpcResponse;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 指数回退等待时常策略
 */
@Slf4j
public class ExponentialBackoffRetryStrategy implements RetryStrategy {

    private final String logPrefix = "[ExponentialBackoffRetryStrategy]: ";

    /**
     * 成倍地降低某个过程的速率，以逐渐找到合适速率的算法
     * @param callable
     * @return
     * @throws Exception
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfExceptionOfType(Exception.class)
                .retryIfRuntimeException()
                .withWaitStrategy(WaitStrategies.exponentialWait(100L, 10,TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(5))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info(logPrefix + "retry count: ", attempt.getAttemptNumber());
                    }
                }).build();
        return retryer.call(callable);
    }
}
