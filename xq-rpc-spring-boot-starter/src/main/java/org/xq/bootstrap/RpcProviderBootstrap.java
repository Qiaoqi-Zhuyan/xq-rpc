package org.xq.bootstrap;

import cn.hutool.core.util.ObjectUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.xq.annotation.RpcService;
import org.xq.xqrpc.RpcApplication;
import org.xq.xqrpc.config.RegistryConfig;
import org.xq.xqrpc.config.RpcServiceConfig;
import org.xq.xqrpc.constant.RpcConstant;
import org.xq.xqrpc.model.ServiceMetaInfo;
import org.xq.xqrpc.registry.LocalRegistry;
import org.xq.xqrpc.registry.Registry;
import org.xq.xqrpc.registry.RegistryFactory;

/**
 * Rpc 服务提供者启动
 */
public class RpcProviderBootstrap implements BeanPostProcessor {

    /**
     * Bean 初始化后执行, 注册服务
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException{
        Class<?> beanClass = bean.getClass();
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);
        if (rpcService != null){
            // 注册服务
            // 获得服务基本信息
            Class<?> interfaceClass = rpcService.interfaceClass();
            if (interfaceClass == void.class){
                interfaceClass = beanClass.getInterfaces()[0];
            }
            String serviceName = interfaceClass.getName();
            String serviceVersion = rpcService.serviceVersion();;
            // 注册本地服务
            LocalRegistry.register(serviceName, beanClass);
            // 注册服务中心
            final RpcServiceConfig rpcServiceConfig = RpcApplication.getConfig();
            RegistryConfig registryConfig = rpcServiceConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(serviceVersion);
            serviceMetaInfo.setServiceHost(rpcServiceConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcServiceConfig.getServerPort());

            try{
                registry.register(serviceMetaInfo);
            }catch (Exception e){
                throw new RuntimeException("[RpcProviderBootstrap]: " + "registry failed", e);
            }
        }

        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }


}
