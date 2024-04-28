package org.xq.xqrpc.registry.registryImpl;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.alibaba.cloud.nacos.discovery.NacosWatch;
import com.alibaba.fastjson2.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.builder.InstanceBuilder;
import io.etcd.jetcd.ByteSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xq.xqrpc.RpcApplication;
import org.xq.xqrpc.config.RegistryConfig;
import org.xq.xqrpc.model.ServiceMetaInfo;
import org.xq.xqrpc.registry.Registry;
import org.xq.xqrpc.registry.RegistryServiceCache;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 基于nacos的服务中心
 */
public class NacosRegistry implements Registry {

    private static final Logger logger = LoggerFactory.getLogger(NacosRegistry.class);

    /**
     * nacos服务器
     */
    private NamingService namingService;

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
    private static final String NACOS_ROOT_PATH = "/rpc/nacos/";


    @Override
    public void init(RegistryConfig registryConfig) {
        try {
            namingService = NacosFactory.createNamingService(registryConfig.getAddress());
        } catch (NacosException e){
            throw new RuntimeException("[NacosRegistry]: ", e);
        }
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 设置存储的键值对
        String registerKey = NACOS_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        namingService.registerInstance(registerKey, serviceMetaInfo.getServiceHost(), serviceMetaInfo.getServicePort());
        // 注册到本地缓存
        localRegisterNodeKeySet.add(registerKey);
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String registerKey = NACOS_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        try{
            namingService.deregisterInstance(registerKey, serviceMetaInfo.getServiceHost(), serviceMetaInfo.getServicePort());
        }catch (NacosException e){
            throw new RuntimeException("[NacosRegistry]: ", e);
        }
        localRegisterNodeKeySet.remove(registerKey);
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) throws NacosException {
        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceCache.readCache();
        if(cachedServiceMetaInfoList != null){
            logger.info("[NacosRegistry]: read from cachedService");
            return cachedServiceMetaInfoList;
        }
        try {
            List<ServiceMetaInfo> serviceMetaInfos = new ArrayList<>();
            List<Instance> instances = namingService.getAllInstances(serviceKey);
            for (Instance instance : instances) {
                serviceMetaInfos.add(toServiceMetaInfo(instance));
            }
            // 写入缓存
            registryServiceCache.writeCache(serviceMetaInfos);
            return serviceMetaInfos;
        } catch (NacosException e){
            throw new RuntimeException("[NacosRegistry]: ", e);
        }
    }

    @Override
    public void destroy() {
        logger.info("[NacosRegistry]: service node destroy");
        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceCache.readCache();
        for (ServiceMetaInfo serviceMetaInfo : cachedServiceMetaInfoList){
          unRegister(serviceMetaInfo);
        }

        localRegisterNodeKeySet.clear();

        try {
            if (namingService != null)
                namingService.shutDown();
        } catch (NacosException e){
            throw new RuntimeException("[NacosRegistry]: ", e);
        }
    }

    @Override
    public void heartBeat() {

    }

    @Override
    public void watch(String serviceNodeKey) {
        String watchKey = NACOS_ROOT_PATH + serviceNodeKey;
        boolean newWatch = watchingKeySet.add(watchKey);
        if (newWatch) {
            try {
                namingService.subscribe(serviceNodeKey, event -> {
                    if (event instanceof NamingEvent){
                    }
                });
            } catch (NacosException e) {
                throw new RuntimeException("[NacosRegistry]: ", e);
            }
        }
    }

    /**
     * 将serviceMetaInfo 转化为 instance
     * @param serviceMetaInfo
     * @return
     */
    public Instance toInstance(ServiceMetaInfo serviceMetaInfo){
        Instance instance = new Instance();
        instance.setServiceName(serviceMetaInfo.getServiceName());
        instance.setIp(serviceMetaInfo.getServiceHost());
        instance.setPort(serviceMetaInfo.getServicePort());
        return instance;
    }

    /**
     * 将instance 转化为 serviceMetaInfo
     * @param instance
     * @return
     */
    public ServiceMetaInfo toServiceMetaInfo(Instance instance){
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(instance.getServiceName());
        serviceMetaInfo.setServicePort(instance.getPort());
        serviceMetaInfo.setServiceHost(instance.getIp());
        return serviceMetaInfo;
    }
}
