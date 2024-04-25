package org.xq.annotation;

import org.springframework.context.annotation.Import;
import org.xq.bootstrap.RpcConsumerBootstrap;
import org.xq.bootstrap.RpcInitBootstrap;
import org.xq.bootstrap.RpcProviderBootstrap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启动RPC注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RpcInitBootstrap.class, RpcProviderBootstrap.class, RpcConsumerBootstrap.class})
public @interface EnableRpc {

    /**
     * 需要启动 server
     * @return
     */
    boolean needServer() default true;
}
