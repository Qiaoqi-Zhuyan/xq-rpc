package org.xq.provider;

import org.xq.common.service.UserService;
import org.xq.xqrpc.registry.LocalRegistry;
import org.xq.xqrpc.server.HttpServer;
import org.xq.xqrpc.server.VertxHttpServer;

/**
 * 服务提供者启动类, 编写服务器提供的代码
 */
public class EasyProviderExample {

    public static void main(String[] args){
        // 注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        // 提供服务HttpServer
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);
    }

}
