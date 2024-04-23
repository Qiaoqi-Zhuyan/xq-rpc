package org.xq.xqrpc.bootstrap;

import org.xq.xqrpc.RpcApplication;
import org.xq.xqrpc.config.RegistryConfig;
import org.xq.xqrpc.config.RpcServiceConfig;
import org.xq.xqrpc.model.ServiceMetaInfo;
import org.xq.xqrpc.model.ServiceRegisterInfo;
import org.xq.xqrpc.registry.LocalRegistry;
import org.xq.xqrpc.registry.Registry;
import org.xq.xqrpc.registry.RegistryFactory;
import org.xq.xqrpc.server.tcp.VertxTcpServer;

import java.util.List;

/**
 *  服务提供者启动类
 */
public class ProviderBootstrap {

    /**
     * 初始化
     * @param serviceRegisterInfoList
     */
    public static void init(List<ServiceRegisterInfo<?>> serviceRegisterInfoList){
        // Rpc 初始化
        RpcApplication.init();
        // 全局变量初始化
        final RpcServiceConfig rpcServiceConfig = RpcApplication.getConfig();

        // 注册服务
        for (ServiceRegisterInfo<?> serviceRegisterInfo : serviceRegisterInfoList){
            String serviceName = serviceRegisterInfo.getServiceName();
            // 本地注册
            LocalRegistry.register(serviceName, serviceRegisterInfo.getImplClass());

            // 注册到服务中心
            RegistryConfig registryConfig = rpcServiceConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceHost(rpcServiceConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcServiceConfig.getServerPort());
            try{
                registry.register(serviceMetaInfo);
            } catch (Exception e){
                throw new RuntimeException("[ProviderBootstrap]: " + "registry failed", e);
            }
        }
        // 启动服务
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(rpcServiceConfig.getServerPort());

    }

}
