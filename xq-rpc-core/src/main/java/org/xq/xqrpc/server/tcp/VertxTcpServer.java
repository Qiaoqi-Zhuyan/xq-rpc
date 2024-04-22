package org.xq.xqrpc.server.tcp;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.slf4j.Slf4j;
import org.xq.xqrpc.server.HttpServer;

/**
 *  1. 创建vert.x服务实例
 *  2. 定义处理方法
 *  3. 启动服务
 *
 *  类似于之前的http请求
 */
@Slf4j
public class VertxTcpServer implements HttpServer {

    private final String logPrefix = "[VertxTcpServer]: ";

    @Override
    public void doStart(int port) {
        // 创建vertx服务
        Vertx vertx = Vertx.vertx();

        // 创建tcp服务器
        NetServer server = vertx.createNetServer();

        server.connectHandler(new TcpServerHandler());

        // 启动tcp服务器并监听端口
        server.listen(port, netServerAsyncResult -> {
            if (netServerAsyncResult.succeeded())
                log.info(logPrefix + "TCP server started on port " + port);
            else
                log.info(logPrefix + "Failed to start TCP server: " + netServerAsyncResult.cause());
        });
    }

}
