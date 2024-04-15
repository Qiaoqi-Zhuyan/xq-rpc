package org.xq.xqrpc.config;

import lombok.Data;

/**
 * 全局配置
 */
@Data
public class RpcServiceConfig {

    /**
     * 名称
     */
    private String ServiceName = "rpc-client";

    /**
     * 版本
     */
    private String version = "1.0";

    /**
     * 服务器主机名
     */
    private String serverHost = "127.0.0.1";

    /**
     * 服务器端口号
     */
    private Integer serverPort = 8080;

    

}
