package org.xq.xqrpc.server.tcp;

import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

/**
 * TCP 客户端
 * 1. 创建vertx客户端
 * 2. 定义处理请求返回
 * 3. 建立连接
 */
@Slf4j
public class VertxTcpClient {

    private final String logPrefix = "[VertxTcpClient]: ";

    public void start(){
        Vertx vertx = Vertx.vertx();

        vertx.createNetClient().connect(8080, "localhost", netSocketAsyncResult -> {
            if (netSocketAsyncResult.succeeded()){
                log.info(logPrefix + "Connect to TCP server");
                io.vertx.core.net.NetSocket socket = netSocketAsyncResult.result();
                // 发送数据
                socket.write("hello world");
                // 接受响应
                socket.handler(buffer -> {
                    log.info(logPrefix + "Received response from server: " + buffer.toString());
                });
            }else
                log.info(logPrefix + "Failed to connect to TCP server");
        });
    }

    public static void main(String args[]){
        new VertxTcpClient().start();
    }

}
