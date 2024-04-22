package org.xq.xqrpc.server.tcp;

import cn.hutool.core.util.IdUtil;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;
import org.xq.xqrpc.RpcApplication;
import org.xq.xqrpc.model.RpcRequest;
import org.xq.xqrpc.model.RpcResponse;
import org.xq.xqrpc.model.ServiceMetaInfo;
import org.xq.xqrpc.protocol.ProtocolMessage;
import org.xq.xqrpc.protocol.ProtocolMessageDecoder;
import org.xq.xqrpc.protocol.ProtocolMessageEncoder;
import org.xq.xqrpc.protocol.utils.ProtocolConstant;
import org.xq.xqrpc.protocol.utils.ProtocolMessageSerializerEnum;
import org.xq.xqrpc.protocol.utils.ProtocolMessageTypeEnum;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * TCP 客户端
 * 1. 创建vertx客户端
 * 2. 定义处理请求返回
 * 3. 建立连接
 */
@Slf4j
public class VertxTcpClient {

    private static final String logPrefix = "[VertxTcpClient]: ";

    /**
     * 处理tcp发送请求
     * @param request
     * @param serviceMetaInfo
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static RpcResponse doRequest(RpcRequest request, ServiceMetaInfo serviceMetaInfo) throws InterruptedException, ExecutionException {
        // 发送tcp请求
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();
        CompletableFuture<RpcResponse> responseCompletableFuture = new CompletableFuture<>();
        netClient.connect(serviceMetaInfo.getServicePort(), serviceMetaInfo.getServiceHost(), netSocketAsyncResult -> {
           if (!netSocketAsyncResult.succeeded()){
               log.error(logPrefix + "Failed to connect to TCP server");
               return;
           }
            NetSocket socket = netSocketAsyncResult.result();
           // 构造消息, 发送
            ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
            ProtocolMessage.Header header = new ProtocolMessage.Header();
            header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
            header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
            header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getConfig().getSerializer()).getKey());
            header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
            // 生成全局ID
            header.setRequestId(IdUtil.getSnowflakeNextId());
            protocolMessage.setHeader(header);
            protocolMessage.setBody(request);

            try{
                Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
                socket.write(encodeBuffer);
            }catch (IOException e){
                throw new RuntimeException("Protocol Message Encode failed");
            }

            // 接受请求
            TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(
                    buffer -> {
                        try {
                            ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                            responseCompletableFuture.complete(rpcResponseProtocolMessage.getBody());
                        }catch (IOException e){
                            throw new RuntimeException("Protocol Message decode failed");
                        }
                    });
            socket.handler(bufferHandlerWrapper);
        });
        RpcResponse rpcResponse = responseCompletableFuture.get();
        netClient.close();
        return rpcResponse;
    }
}
