package org.xq.xqrpc.registry.registryImpl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import com.alibaba.fastjson2.JSON;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xq.xqrpc.model.ServiceMetaInfo;
import org.xq.xqrpc.registry.Registry;
import org.xq.xqrpc.config.RegistryConfig;
import org.xq.xqrpc.registry.RegistryServiceCache;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class EtcdRegistry implements Registry {

    private static final Logger logger = LoggerFactory.getLogger(EtcdRegistry.class);
    private Client client;

    /**
     * 键值客户端
     */
    private KV kvClient;

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
    private static final String ETCD_ROOT_PATH = "/rpc/etcd/";

    /**
     * 初始化
     * @param registryConfig
     */
    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();
        heartBeat();
    }

    /**
     * 服务注册, 创建key并设置过期时间, value为服务注册信息的json序列化
     * @param serviceMetaInfo
     * @throws Exception
     */
    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 创建lease和kv客户端
        Lease leaseClient = client.getLeaseClient();

        // 创建一个30s的租约
        long leaseId = leaseClient.grant(30).get().getID();

        // 设置存储的键值对
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSON.toJSONString(serviceMetaInfo), StandardCharsets.UTF_8);

        // 将键值与租约关联, 设置过期时间
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();

        localRegisterNodeKeySet.add(registerKey);
    }

    /**
     * 服务注销, 删除key
     * @param serviceMetaInfo
     */
    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        kvClient.delete(ByteSequence.from(ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey(), StandardCharsets.UTF_8));
        localRegisterNodeKeySet.remove(registerKey);
    }

    /**
     * 服务发现, 根据服务名称作为前缀, 从Etcd获取服务下的节点列表
     *
     * 逻辑: 优先从缓存获取服务, 如果没有缓存, 再从服务注册中心获取, 并设置到缓存中
     *
     * @param serviceKey
     * @return
     */
    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        // 优先读缓存
        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceCache.readCache();
        if (cachedServiceMetaInfoList != null) {
            logger.info("[EtcdRegistry]: read from cachedService");
            return cachedServiceMetaInfoList;
        }
        String searchPrefix = ETCD_ROOT_PATH + serviceKey + "/";

        try{
            // 前缀查询
            GetOption getOption = GetOption.builder()
                    .isPrefix(true)
                    .build();
            List<KeyValue> keyValues = kvClient.get(ByteSequence.from(searchPrefix, StandardCharsets.UTF_8), getOption)
                    .get()
                    .getKvs();
            // 解析服务信息
            List<ServiceMetaInfo> serviceMetaInfoList = keyValues.stream()
                    .map(keyValue -> {
                        String key = keyValue.getKey().toString(StandardCharsets.UTF_8);
                        watch(key);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        return JSON.parseObject(value, ServiceMetaInfo.class);
                    }).collect(Collectors.toList());
            // 存入缓存
            registryServiceCache.writeCache(serviceMetaInfoList);
            return serviceMetaInfoList;
        }catch (Exception e){
            throw new RuntimeException("[EtcdRegistry]: acquire service failed", e);
        }
    }

    /**
     * 注册中心销毁
     * 项目关闭后释放资源
     */
    @Override
    public void destroy() {
        logger.info("[EtcdRegistry]: Node destroy");

        for(String key: localRegisterNodeKeySet){
            try {
                kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
            } catch (Exception e){
                throw new RuntimeException(key + " node destroy off");
            }
        }

        if (kvClient != null)
            kvClient.close();
        if (kvClient != null)
            client.close();
    }

    @Override
    public void heartBeat() {
        // 10s 续签一次
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                for (String key : localRegisterNodeKeySet){
                    try{
                        List<KeyValue> keyValues = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8))
                                .get()
                                .getKvs();
                        // 节点过期, 重启节点重新注册
                        if (CollUtil.isEmpty(keyValues)){
                            continue;
                        }
                        // 节点未过期, 重新注册
                        KeyValue keyValue = keyValues.get(0);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSON.parseObject(value, ServiceMetaInfo.class);
                        register(serviceMetaInfo);
                    }catch (Exception e){
                        throw new RuntimeException(key + " renew failed", e);
                    }
                }
            }
        });

        CronUtil.setMatchSecond(true);
        CronUtil.start();

    }

    /**
     * watch 由服务端执行, 缓存在消费服务端进行维护使用, 在消费者执行方法中
     * 创建watch监听器
     * @param serviceNodeKey
     */
    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();
        boolean newWatch = watchingKeySet.add(serviceNodeKey);
        if (newWatch) {
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), watchResponse -> {
                for (WatchEvent event : watchResponse.getEvents()) {
                    switch (event.getEventType()) {
                        case DELETE:
                            registryServiceCache.clearCache();
                            break;
                        case PUT:
                        default:
                            break;
                    }
                }
            });
        }
    }
}
