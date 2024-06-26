package org.xq.xqrpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import lombok.extern.slf4j.Slf4j;
import org.xq.xqrpc.RpcApplication;
import org.xq.xqrpc.config.RpcServiceConfig;
import org.xq.xqrpc.constant.RpcConstant;
import org.xq.xqrpc.fault.retry.RetryStrategy;
import org.xq.xqrpc.fault.retry.RetryStrategyFactory;
import org.xq.xqrpc.fault.tolerant.TolerantStrategy;
import org.xq.xqrpc.fault.tolerant.TolerantStrategyFactory;
import org.xq.xqrpc.loadBalancer.LoadBalancer;
import org.xq.xqrpc.loadBalancer.LoadBalancerFactory;
import org.xq.xqrpc.model.RpcRequest;
import org.xq.xqrpc.model.RpcResponse;
import org.xq.xqrpc.model.ServiceMetaInfo;

import org.xq.xqrpc.registry.Registry;
import org.xq.xqrpc.registry.RegistryFactory;
import org.xq.xqrpc.serializer.Serializer;
import org.xq.xqrpc.serializer.SerializerFactory;
import org.xq.xqrpc.server.tcp.VertxTcpClient;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 服务代理 jdk 动态代理
 */
@Slf4j
public class ServiceProxy implements InvocationHandler {

    private final String logPrefix = "[ServiceProxy]: ";

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
            RpcServiceConfig rpcServiceConfig = RpcApplication.getConfig();
            Registry registry = RegistryFactory.getInstance(rpcServiceConfig.getRegistryConfig().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
            List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
            if (CollUtil.isEmpty(serviceMetaInfoList)) {
                throw new RuntimeException("no such service address");
            }
            // 负载均衡
            LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcServiceConfig.getLoadBalancer());
            // 将调用方法名作为负载均衡参数 - 调用相同的方法，hashcode一定相同
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("methodName", rpcRequest.getMethodName());
            ServiceMetaInfo selectServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);
            log.info(logPrefix + "service address " + selectServiceMetaInfo.getServiceAddress());
            //发送tcp请求
            RpcResponse rpcResponse = new RpcResponse();
            try{
                // 重试机制
                RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcServiceConfig.getRetryStrategy());
                rpcResponse = retryStrategy.doRetry(() -> VertxTcpClient.doRequest(rpcRequest, selectServiceMetaInfo));
            }catch (Exception e){
                // 重试容错机制
                TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcServiceConfig.getTolerantStrategy());
                rpcResponse = tolerantStrategy.doTolerant(null, e);
            }
            return rpcResponse.getData();
        }catch (Exception e){
            throw new RuntimeException(logPrefix + "invoke failed");
        }
    }

    /**
     * 发送http请求
     * @param selectedServiceMetaInfo
     * @param bodyBytes
     * @return
     * @throws IOException
     */
    private static RpcResponse doHttpRequest(ServiceMetaInfo selectedServiceMetaInfo, byte[] bodyBytes) throws IOException{
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getConfig().getSerializer());
        // 发送http请求
        try (HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
                .body(bodyBytes)
                .execute()){
            byte[] result = httpResponse.bodyBytes();
            // 反序列
            RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
            return rpcResponse;
        }
    }
}
