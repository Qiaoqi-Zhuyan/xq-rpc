package org.xq.xqrpc.server;

/**
 * http 服务器接口
 */
public interface Server {

    /**
     * 启动服务器
     * @param port
     */
    void doStart(int port);
}
