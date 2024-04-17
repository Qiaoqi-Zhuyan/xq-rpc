package org.xq.xqrpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.xq.xqrpc.RpcApplication;
import org.xq.xqrpc.config.RpcServiceConfig;
import org.xq.xqrpc.constant.RpcConstant;
import org.xq.xqrpc.model.RpcRequest;
import org.xq.xqrpc.model.RpcResponse;
import org.xq.xqrpc.model.ServiceMetaInfo;
import org.xq.xqrpc.registry.Registry;
import org.xq.xqrpc.registry.RegistryFactory;
import org.xq.xqrpc.serializer.FastJsonSerializer.FastJsonSerializer;
import org.xq.xqrpc.serializer.Serializer;
import org.xq.xqrpc.serializer.JdkSerializer.JdkSerializer;
import org.xq.xqrpc.serializer.SerializerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 服务代理 jdk 动态代理
 */
public class ServiceProxy implements InvocationHandler {

    /**
     *调用代理类
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 指定序列化器
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getConfig().getSerializer());
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try{
            // 序列化
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            // 发送请求
            // 地址使用注册中心和服务发现机制解决
            RpcServiceConfig rpcServiceConfig = RpcApplication.getConfig();
            Registry registry = RegistryFactory.getInstance(rpcServiceConfig.getRegistryConfig().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
            List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
            if(CollUtil.isEmpty(serviceMetaInfoList)){
                throw new RuntimeException("no such service address");
            }

            ServiceMetaInfo selectedServiceMetaInfo = serviceMetaInfoList.get(0);
            String serviceAddress = selectedServiceMetaInfo.getServiceAddress();
            System.out.println("[ServiceProxy]: " + serviceAddress);
            try(HttpResponse httpResponse = HttpRequest.post(serviceAddress)
                    .body(bodyBytes)
                    .execute()
            ){
                byte[] result = httpResponse.bodyBytes();
                // 反序列化
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return rpcResponse.getData();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }


}
