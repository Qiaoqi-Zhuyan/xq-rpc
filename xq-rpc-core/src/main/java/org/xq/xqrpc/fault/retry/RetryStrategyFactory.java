package org.xq.xqrpc.fault.retry;

import org.xq.xqrpc.fault.retry.retryImpl.FixedIntervalRetryStrategy;
import org.xq.xqrpc.spi.SpiLoader;

/**
 * 重试策略工厂 - 获得重试器的实例
 */
public class RetryStrategyFactory {

    /**
     * 加载重试器对象
     */
    private static class RetryStrategyHolder{
        private static RetryStrategy loadRetryStrategy(String key){
            SpiLoader.load(RetryStrategy.class);
            return SpiLoader.getInstance(RetryStrategy.class, key);
        }
    }

    /**
     * 默认重试器
     */
    private static final RetryStrategy DEFAULT_RETRY_STRATEGY = new FixedIntervalRetryStrategy();

    /**
     * 获取实例
     * @param key
     * @return
     */
    public static RetryStrategy getInstance(String key){
        return RetryStrategyHolder.loadRetryStrategy(key);
    }

}
