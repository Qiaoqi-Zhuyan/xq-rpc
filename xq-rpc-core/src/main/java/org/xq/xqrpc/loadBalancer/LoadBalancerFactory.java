package org.xq.xqrpc.loadBalancer;

import org.xq.xqrpc.loadBalancer.LoadBalancerImpl.RoundRobinLoadBalancer;
import org.xq.xqrpc.spi.SpiLoader;

/**
 * 负载均衡器工厂 - 获取负载均衡器对象
 */
public class LoadBalancerFactory {

    /**
     * 加载负载均衡器
     */
    private static class LoadBalancerSingleton{
        private static LoadBalancer loadSerializer(String key){
            SpiLoader.load(LoadBalancer.class);
            return SpiLoader.getInstance(LoadBalancer.class, key);
        }
    }

    /**
     * 默认负载均衡器
     */
    private static final LoadBalancer DEFAULT_LOAD_BALANCER = new RoundRobinLoadBalancer();

    /**
     * 获取实例
     * @param key
     * @return
     */
    public static LoadBalancer getInstance(String key){
        return LoadBalancerSingleton.loadSerializer(key);
    }
}
