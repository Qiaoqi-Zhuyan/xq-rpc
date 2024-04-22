package org.xq.xqrpc.loadBalancer.LoadBalancerImpl;

import cn.hutool.crypto.digest.DigestUtil;
import org.xq.xqrpc.loadBalancer.LoadBalancer;
import org.xq.xqrpc.model.ServiceMetaInfo;

import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 一致性hash负载均衡器
 */
public class ConsistentHashLoadBalancer implements LoadBalancer {

    /**
     * TreeMap实现一致性Hash环, 存放虚拟节点
     */
    private final TreeMap<Integer, ServiceMetaInfo> virtualNotes = new TreeMap<>();

    private static final int VIRTUAL_NODE_NUM = 100;

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (serviceMetaInfoList.isEmpty())
            return null;

        // 构建虚拟节点环
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList){
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++){
                int hash = getHash(serviceMetaInfo.getServiceAddress() + "#" + i);
                virtualNotes.put(hash, serviceMetaInfo);
            }
        }

        // 获取调用请求的hash值
        int hash = getHash(requestParams);

        // 选择最接近且大于等于调用请求hash值的虚拟节点
        Map.Entry<Integer, ServiceMetaInfo> entry = virtualNotes.ceilingEntry(hash);
        if (entry == null)
            entry = virtualNotes.firstEntry(); // 如果没有大于等于调用请求hash值的虚拟节点, 则返回首节点
        return entry.getValue();
    }

    private int getHash(Object key){
        return key.hashCode();
    }
}
