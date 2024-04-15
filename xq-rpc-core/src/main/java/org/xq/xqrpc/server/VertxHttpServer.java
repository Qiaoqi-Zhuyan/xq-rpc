package org.xq.xqrpc.server;

import io.vertx.core.Vertx;

public class VertxHttpServer implements HttpServer{

    /**
     * 启动服务
     * @param port
     */
    public void doStart(int port) {
        // 创建 Vert.x 实例
        Vertx vertx = Vertx.vertx();
        // 创建http服务器
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();

        // 监听端口并处理
        server.requestHandler(new HttpServerHandler());

        // 启动http服务器并监听指定端口
        server.listen(port, result->{
            if (result.succeeded()){
                System.out.println("Server is listening on port " + port);
            }else{
                System.out.println("Failed to start server: " + result.cause());
            }
        });
    }
}

