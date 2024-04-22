package org.xq.xqrpc.loadBalancer.LoadBalancerImpl;

import org.xq.xqrpc.loadBalancer.LoadBalancer;
import org.xq.xqrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡器
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

    /**
     * 当前轮询的下标
     *
     * 原子计数, 保证并发
     */
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (serviceMetaInfoList.isEmpty()){
            return null;
        }
        // 只有一个服务
        if (serviceMetaInfoList.size() == 1)
            return serviceMetaInfoList.get(0);
        // 取模轮询
        int index = currentIndex.getAndIncrement() % serviceMetaInfoList.size();
        return serviceMetaInfoList.get(index);
    }
}
