package org.xq.xqrpc;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xq.xqrpc.config.RegistryConfig;
import org.xq.xqrpc.config.RpcServiceConfig;
import org.xq.xqrpc.constant.RpcConstant;
import org.xq.xqrpc.registry.Registry;
import org.xq.xqrpc.registry.RegistryFactory;
import org.xq.xqrpc.spi.SpiLoader;
import org.xq.xqrpc.utils.ConfigUtils;

/**
 * rpc服务入口
 */
@Slf4j
public class RpcApplication {
    private static final Logger logger = LoggerFactory.getLogger(RpcApplication.class);

    private static volatile RpcServiceConfig rpcServiceConfig;
    /**
     * 框架初始化, 支持传入自定义配置
     * @param serviceConfig
     */
    public static void init(RpcServiceConfig serviceConfig){
        rpcServiceConfig = serviceConfig;
        logger.info("[RpcApplication]: Rpc init, config = {}", serviceConfig.toString());
        // 注册中心
        RegistryConfig registryConfig = rpcServiceConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        registry.init(registryConfig);
        logger.info("[RpcApplication]: registry init, config = {}", registryConfig);
        // 创建并注册shutdown hook, jvm 退出时执行
        Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
    }

    /**
     * 初始化
     */
    public static void init(){
        RpcServiceConfig serviceConfig;
        try{
            serviceConfig = ConfigUtils.loadConfig(RpcConstant.YAML_CONFIG_FILE, RpcServiceConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        }catch (Exception e){
            // 使用默认值
            e.printStackTrace();
            serviceConfig = new RpcServiceConfig();
        }
        init(serviceConfig);
    }

    /**
     * 获取配置 - 双检锁单例模式
     * @return
     */
    public static RpcServiceConfig getConfig(){
        if (rpcServiceConfig == null){
            synchronized (RpcApplication.class){
                if (rpcServiceConfig == null){
                    init();
                }
            }
        }
        return rpcServiceConfig;
    }
}
