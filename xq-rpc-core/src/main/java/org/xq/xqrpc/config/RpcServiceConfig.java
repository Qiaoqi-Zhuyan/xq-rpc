package org.xq.xqrpc.config;

import lombok.Data;
import org.xq.xqrpc.fault.retry.RetryStrategyKeys;
import org.xq.xqrpc.loadBalancer.LoadBalancerKeys;
import org.xq.xqrpc.serializer.SerializerKeys;

/**
 * 全局配置
 */
@Data
public class RpcServiceConfig {

    /**
     * 名称
     */
    private String ServiceName = "rpc-client";

    /**
     * 版本
     */
    private String version = "1.0";

    /**
     * 服务器主机名
     */
    private String serverHost = "127.0.0.1";

    /**
     * 服务器端口号
     */
    private Integer serverPort = 8080;

    /**
     * 模拟调用Mock
     */
    private boolean mock = false;

    /**
     * 序列化器
     */
    private String serializer = SerializerKeys.FastJson;

    /**
     * 负载均衡器
     */
    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN;

    /**
     * 重试策略
     */
    private String retryStrategy = RetryStrategyKeys.FIXED_INTERVAL;

    /**
     * 注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();
}
