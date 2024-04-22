package org.xq.xqrpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import lombok.extern.slf4j.Slf4j;
import org.xq.xqrpc.RpcApplication;
import org.xq.xqrpc.config.RpcServiceConfig;
import org.xq.xqrpc.constant.RpcConstant;
import org.xq.xqrpc.model.RpcRequest;
import org.xq.xqrpc.model.RpcResponse;
import org.xq.xqrpc.model.ServiceMetaInfo;
import org.xq.xqrpc.protocol.ProtocolMessage;
import org.xq.xqrpc.protocol.ProtocolMessageDecoder;
import org.xq.xqrpc.protocol.ProtocolMessageEncoder;
import org.xq.xqrpc.protocol.utils.ProtocolConstant;
import org.xq.xqrpc.protocol.utils.ProtocolMessageSerializerEnum;
import org.xq.xqrpc.protocol.utils.ProtocolMessageTypeEnum;
import org.xq.xqrpc.registry.Registry;
import org.xq.xqrpc.registry.RegistryFactory;
import org.xq.xqrpc.serializer.FastJsonSerializer.FastJsonSerializer;
import org.xq.xqrpc.serializer.Serializer;
import org.xq.xqrpc.serializer.JdkSerializer.JdkSerializer;
import org.xq.xqrpc.serializer.SerializerFactory;
import org.xq.xqrpc.server.tcp.VertxTcpClient;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;

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
            ServiceMetaInfo selectServiceMetaInfo = serviceMetaInfoList.get(0);
            //发送tcp请求
            RpcResponse rpcResponse = VertxTcpClient.doRequest(rpcRequest, selectServiceMetaInfo);
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
