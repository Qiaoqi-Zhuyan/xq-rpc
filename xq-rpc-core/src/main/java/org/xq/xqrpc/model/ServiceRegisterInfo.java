package org.xq.xqrpc.model;

import lombok.Data;

/**
 * 服务注册信息
 * @param <T>
 */
@Data
public class ServiceRegisterInfo<T> {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 实现类
     */
    private Class<? extends T> implClass;

}
