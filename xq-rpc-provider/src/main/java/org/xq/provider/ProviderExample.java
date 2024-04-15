package org.xq.provider;

import org.xq.common.service.UserService;
import org.xq.xqrpc.RpcApplication;
import org.xq.xqrpc.registry.LocalRegistry;
import org.xq.xqrpc.serializer.Serializer;
import org.xq.xqrpc.server.HttpServer;
import org.xq.xqrpc.server.VertxHttpServer;

import java.util.ServiceLoader;

/**
 * 服务提供者示意
 */
public class ProviderExample {
    public static void main(String[] args){
        // rpc框架初始化
        RpcApplication.init();

        // 注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        // 启动web服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(RpcApplication.getConfig().getServerPort());

    }
}
