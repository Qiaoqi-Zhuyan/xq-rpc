package org.xq.xqrpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

/**
 * 服务注册信息
 * @param <T>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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
