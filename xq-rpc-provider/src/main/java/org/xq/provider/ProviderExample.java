package org.xq.provider;

import org.xq.common.service.UserService;
import org.xq.xqrpc.RpcApplication;
import org.xq.xqrpc.config.RegistryConfig;
import org.xq.xqrpc.config.RpcServiceConfig;
import org.xq.xqrpc.model.ServiceMetaInfo;
import org.xq.xqrpc.registry.LocalRegistry;
import org.xq.xqrpc.registry.Registry;
import org.xq.xqrpc.registry.RegistryFactory;
import org.xq.xqrpc.serializer.Serializer;
import org.xq.xqrpc.server.HttpServer;
import org.xq.xqrpc.server.VertxHttpServer;
import org.xq.xqrpc.server.tcp.VertxTcpClient;
import org.xq.xqrpc.server.tcp.VertxTcpServer;

import java.util.Date;
import java.util.ServiceLoader;

/**
 * 服务提供者示意
 */
public class ProviderExample {
    public static void main(String[] args){
        // rpc框架初始化
        RpcApplication.init();


        // 注册服务
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName, UserServiceImpl.class);

        RpcServiceConfig rpcServiceConfig = RpcApplication.getConfig();
        RegistryConfig registryConfig = RpcApplication.getConfig().getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceHost(rpcServiceConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcServiceConfig.getServerPort());
        serviceMetaInfo.setRegistryTime(new Date());
        try{
            registry.register(serviceMetaInfo);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        System.out.println(serviceMetaInfo.getServiceKey());
        // 启动web服务
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(RpcApplication.getConfig().getServerPort());
    }
}
