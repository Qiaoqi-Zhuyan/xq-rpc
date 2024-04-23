package org.xq.xqrpc.fault.retry.retryImpl;

import com.github.rholder.retry.*;

import lombok.extern.slf4j.Slf4j;
import org.xq.xqrpc.fault.retry.RetryStrategy;
import org.xq.xqrpc.model.RpcResponse;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 固定时间间隔重试策略
 *
 */
@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy {

    private final String logPrefix = "[FixedIntervalRetryStrategy]: ";

    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfExceptionOfType(Exception.class)
                .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info(logPrefix + "retry count: ", attempt.getAttemptNumber());
                    }
                }).build();
        return retryer.call(callable);
    }
}
