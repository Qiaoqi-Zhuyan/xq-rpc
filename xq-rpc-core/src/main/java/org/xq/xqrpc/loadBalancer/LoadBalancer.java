package org.xq.xqrpc.loadBalancer;

import org.xq.xqrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;

/**
 * 负债均衡器通用接口 - 消费端
 */
public interface LoadBalancer {

    /**
     * 选择服务调用
     * @param requestParams
     * @param serviceMetaInfoList
     * @return
     */
    ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList);

}
