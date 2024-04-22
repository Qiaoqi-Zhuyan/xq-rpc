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
        //            // 序列化
//            byte[] bodyBytes = serializer.serialize(rpcRequest);
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
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();
        CompletableFuture<RpcResponse> responseCompletableFuture = new CompletableFuture<>();
        netClient.connect(selectedServiceMetaInfo.getServicePort(), selectedServiceMetaInfo.getServiceHost(), netSocketAsyncResult -> {
           if (netSocketAsyncResult.succeeded()){
               log.info(logPrefix + "Connect to Tcp server");
               io.vertx.core.net.NetSocket netSocket = netSocketAsyncResult.result();
               // 构造消息, 发送请求
               ProtocolMessage.Header header = new ProtocolMessage.Header();
               header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
               header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
               header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getConfig().getSerializer()).getKey());
               header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
               header.setRequestId(IdUtil.getSnowflakeNextId());
               ProtocolMessage<RpcRequest> requestProtocolMessage = new ProtocolMessage<>(header, rpcRequest);
               // 对请求进行编码
               try{
                   Buffer encodeBuffer = ProtocolMessageEncoder.encode(requestProtocolMessage);
                   netSocket.write(encodeBuffer);
               }catch (IOException e){
                   throw new RuntimeException("protocol message encode fail");
               }
               // 接受响应, 阻塞式
               netSocket.handler(buffer -> {
                    try {
                        ProtocolMessage<RpcResponse> responseProtocolMessage = (ProtocolMessage<RpcResponse>)ProtocolMessageDecoder.decode(buffer);
                        responseCompletableFuture.complete(responseProtocolMessage.getBody());
                    }catch (IOException e){
                        throw new RuntimeException(logPrefix + "protocol decode fail");
                    }
               });
           }else{
               log.info(logPrefix + "fail to connect TCP server");
           }
        });
        RpcResponse rpcResponse = responseCompletableFuture.get();
        netClient.close();
        return rpcResponse.getData();

    }
}
