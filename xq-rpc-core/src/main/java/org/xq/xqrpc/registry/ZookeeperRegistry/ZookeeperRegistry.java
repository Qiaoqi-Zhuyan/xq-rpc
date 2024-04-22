package org.xq.xqrpc.registry.ZookeeperRegistry;

import cn.hutool.core.collection.ConcurrentHashSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xq.xqrpc.config.RegistryConfig;
import org.xq.xqrpc.model.ServiceMetaInfo;
import org.xq.xqrpc.registry.Registry;
import org.xq.xqrpc.registry.RegistryServiceCache;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于zookeeper的注册中心实现, 注册服务为临时节点
 */
@Slf4j
public class ZookeeperRegistry implements Registry {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperRegistry.class);

    private CuratorFramework client;

    private ServiceDiscovery<ServiceMetaInfo> serviceDiscovery;

    /**
     * 本机注册缓存的节点key集合,用于维护续期
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();

    /**
     * 注册中心服务缓存
     */
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();

    /**
     *  正在监听的key的集合
     *  防止重复监听同一个key
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

    /**
     * 根节点
     */
    private static final String ZK_ROOT_PATH = "/rpc/zk/";

    @Override
    public void init(RegistryConfig registryConfig) {
        // 构建client 和 serviceDiscovery
        client = CuratorFrameworkFactory
                .builder()
                .connectString(registryConfig.getAddress())
                .retryPolicy(new ExponentialBackoffRetry(Math.toIntExact(registryConfig.getTimeout()), 3))
                .build();

        serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMetaInfo.class)
                .client(client)
                .basePath(ZK_ROOT_PATH)
                .serializer(new JsonInstanceSerializer<>(ServiceMetaInfo.class))
                .build();

        try{
            client.start();
            serviceDiscovery.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 注册到zk中
        serviceDiscovery.registerService(serviceInstanceBuilder(serviceMetaInfo));

        // 注册到本地缓存
        String registerKey = ZK_ROOT_PATH + "/" + serviceMetaInfo.getServiceNodeKey();
        localRegisterNodeKeySet.add(registerKey);
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        try{
            serviceDiscovery.unregisterService(serviceInstanceBuilder(serviceMetaInfo));
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        // 本地缓存
        String registerKey = ZK_ROOT_PATH + "/" + serviceMetaInfo.getServiceNodeKey();
        localRegisterNodeKeySet.remove(registerKey);
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceCache.readCache();
        if (cachedServiceMetaInfoList != null){
            logger.info("[EtcdRegistry]: read from cachedService");
            return cachedServiceMetaInfoList;
        }

        try{
            Collection<ServiceInstance<ServiceMetaInfo>> serviceInstanceCollection = serviceDiscovery.queryForInstances(serviceKey);

            List<ServiceMetaInfo> serviceMetaInfoList = serviceInstanceCollection.stream()
                    .map(ServiceInstance::getPayload)
                    .collect(Collectors.toList());

            // 写入缓存
            registryServiceCache.writeCache(serviceMetaInfoList);
            return serviceMetaInfoList;
        }catch (Exception e){
            throw new RuntimeException("[ZookeeperRegistry]: acquire service list fail");
        }
    }

    @Override
    public void destroy() {
        logger.info("[ZookeeperRegistry]: service node destroy");

        for (String key: localRegisterNodeKeySet){
            try{
                client.delete().guaranteed().forPath(key);
            }catch (Exception e){
                throw new RuntimeException(key + " service destroy failed ", e);
            }
        }

        if (client != null)
            client.close();
    }

    @Override
    public void heartBeat() {

    }

    @Override
    public void watch(String serviceNodeKey) {
        String watchKey = ZK_ROOT_PATH + "/" + serviceNodeKey;
        boolean newWatch = watchingKeySet.add(watchKey);
        if (newWatch){
            CuratorCache curatorCache = CuratorCache.build(client, watchKey);
            curatorCache.start();
            curatorCache.listenable().addListener(
                    CuratorCacheListener
                            .builder()
                            .forDeletes(childData -> {
                                registryServiceCache.clearCache();
                            })
                            .forChanges((oldNode, node) -> {
                                registryServiceCache.clearCache();
                            })
                            .build()
            );
        }
    }

    /**
     * 构建zookeeper的服务实例进行注册
     * @param serviceMetaInfo
     * @return
     */
    private ServiceInstance<ServiceMetaInfo> serviceInstanceBuilder(ServiceMetaInfo serviceMetaInfo){
        String serviceAddress = serviceMetaInfo.getServiceHost() + ":" + serviceMetaInfo.getServicePort();
        try {
            return ServiceInstance.<ServiceMetaInfo>builder()
                    .id(serviceAddress)
                    .name(serviceMetaInfo.getServiceKey())
                    .address(serviceAddress)
                    .payload(serviceMetaInfo)
                    .build();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
