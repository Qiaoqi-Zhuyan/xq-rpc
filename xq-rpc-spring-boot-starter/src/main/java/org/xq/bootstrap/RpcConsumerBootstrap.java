package org.xq.bootstrap;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.xq.annotation.RpcReference;
import org.xq.xqrpc.proxy.ServiceProxyFactory;

import java.lang.reflect.Field;

/**
 * rpc 服务消费启动
 */
@Slf4j
public class RpcConsumerBootstrap implements BeanPostProcessor {

    /**
     * bean 初始化后执行, 注入服务
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        // 遍历所有对象
        Field[] declareFields = beanClass.getDeclaredFields();
        for (Field field : declareFields){
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (ObjectUtil.isNotNull(rpcReference)){
                // 为属性生成代理对象
                Class<?> interfaceClass = rpcReference.interfaceClass();
                if (interfaceClass == void.class){
                    interfaceClass = field.getType();
                }
                field.setAccessible(true);
                Object proxyObject = ServiceProxyFactory.getProxy(interfaceClass);
                try{
                    field.set(bean, proxyObject);
                    field.setAccessible(false);
                }catch (IllegalAccessException e){
                    throw new RuntimeException("[RpcConsumerBootstrap]: ", e);
                }
            }
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

}
