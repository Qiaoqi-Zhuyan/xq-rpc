package org.xq.xqrpc.registry;

import org.xq.xqrpc.config.RegistryConfig;
import org.xq.xqrpc.model.ServiceMetaInfo;

import java.util.List;

/**
 * 注册中心
 */
public interface Registry {

    /**
     * 初始化
     * @param registryConfig
     */
    public void init(RegistryConfig registryConfig);

    /**
     * 注册服务 (服务端)
     * @param serviceMetaInfo
     * @throws Exception
     */
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception;

    /**
     * 注销服务 (服务端)
     * @param serviceMetaInfo
     */
    public void unRegister(ServiceMetaInfo serviceMetaInfo);

    /**
     * 服务发现 (获取某服务的所有节点, 消费端)
     * @param serviceKey
     * @return
     */
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey);

    /**
     * 服务销毁
     */
    public void destroy();

    /**
     * 心跳检测 服务端
     */
    void heartBeat();
}
