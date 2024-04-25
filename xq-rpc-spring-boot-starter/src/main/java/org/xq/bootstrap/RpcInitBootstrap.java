package org.xq.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.xq.annotation.EnableRpc;
import org.xq.xqrpc.RpcApplication;
import org.xq.xqrpc.config.RpcServiceConfig;
import org.xq.xqrpc.server.tcp.VertxTcpServer;

/**
 * RPC 框架启动
 */
@Slf4j
public class RpcInitBootstrap implements ImportBeanDefinitionRegistrar {

    /**
     * Spring 初始化执行, 初始化rpc框架
     * @param importingClassMetadata
     * @param registry
     */
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry){
        // 获取EnableRpc 注解的属性值
        boolean needServer = (boolean) importingClassMetadata.getAnnotationAttributes(EnableRpc.class.getName()).get("needServer");

        // RPC 框架初始化 - 配置和注册中心
        RpcApplication.init();

        // 全局变量
        final RpcServiceConfig rpcServiceConfig = RpcApplication.getConfig();

        // 启动服务
        if (needServer){
            VertxTcpServer vertxTcpServer = new VertxTcpServer();
            vertxTcpServer.doStart(rpcServiceConfig.getServerPort());
        }else {
            log.info("[RpcInitBootstrap]: " + "server stop");
        }
    }

}
