package org.xq.xqrpc.fault.retry;

/**
 * 重试策略键名
 */
public class RetryStrategyKeys {

    /**
     * 不进行重试
     */
    public static final String NO = "no";

    /**
     * 固定时间间隔
     */
    public static final String FIXED_INTERVAL = "fixedInterval";

    /**
     * 指数回退
     */
    public static final String EXPONENTIAL_BACKOFF = "exponentialBackoff";


}
