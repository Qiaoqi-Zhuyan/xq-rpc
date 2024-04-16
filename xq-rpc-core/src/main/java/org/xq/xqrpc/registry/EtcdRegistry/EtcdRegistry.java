package org.xq.xqrpc.registry.EtcdRegistry;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import lombok.extern.slf4j.Slf4j;
import org.xq.xqrpc.model.ServiceMetaInfo;
import org.xq.xqrpc.registry.Registry;
import org.xq.xqrpc.config.RegistryConfig;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class EtcdRegistry implements Registry {

    private Client client;

    /**
     * 键值客户端
     */
    private KV kvClient;

    /**
     * 根节点
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";

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
        long leaseId = leaseClient.grant(300).get().getID();

        // 设置存储的键值对
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        // 将键值与租约关联, 设置过期时间
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();
    }

    /**
     * 服务注销, 删除key
     * @param serviceMetaInfo
     */
    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        kvClient.delete(ByteSequence.from(ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey(), StandardCharsets.UTF_8));
    }

    /**
     * 服务发现, 根据服务名称作为前缀, 从Etcd获取服务下的节点列表
     * @param serviceKey
     * @return
     */
    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
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
            return keyValues.stream()
                    .map(keyValue -> {
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    }).collect(Collectors.toList());
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
        log.info("[EtcdRegistry]: Node destroy");
        if (kvClient != null)
            kvClient.close();
        if (kvClient != null)
            client.close();
    }
}
