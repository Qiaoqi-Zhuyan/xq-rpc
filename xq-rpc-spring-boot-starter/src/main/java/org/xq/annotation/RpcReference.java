package org.xq.annotation;

import org.xq.xqrpc.constant.RpcConstant;
import org.xq.xqrpc.fault.retry.RetryStrategyKeys;
import org.xq.xqrpc.fault.tolerant.TolerantStrategyKeys;
import org.xq.xqrpc.loadBalancer.LoadBalancerKeys;

/**
 * 服务消费者注解 - 用于注入服务
 *
 * 需要指定调用服务相关属性, 服务接口类 , 可能存在多个接口, 版本号, 负载均衡器, 重试策略等
 */
public @interface RpcReference {

    /**
     * 服务接口类
     * @return
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 版本
     * @return
     */
    String serviceVersion() default RpcConstant.DEFAULT_SERVICE_VERSION;

    /**
     * 负载均衡器
     * @return
     */
    String loadBalancer() default LoadBalancerKeys.ROUND_ROBIN;

    /**
     * 重试策略
     * @return
     */
    String retryStrategy() default RetryStrategyKeys.FIXED_INTERVAL;

    /**
     * 容错策略
     * @return
     */
    String tolerantStrategy() default TolerantStrategyKeys.FAIL_FAST;

    /**
     * 模拟调用
     * @return
     */
    boolean mock() default false;
}
