package org.xq.xqrpc.config;

import lombok.Data;

/**
 * rpc框架注册中心配置
 */
@Data
public class RegistryConfig {

    /**
     * 注册中心
     */
    private String registry = "etcd";

    /**
     * 注册中心地址
     */
    private String address = "http://localhost:2380";

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 超时时间 ms
     */
    private Long timeout = 10000L;

}
