package org.xq.xqrpc.server.tcp;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import org.xq.xqrpc.model.RpcRequest;
import org.xq.xqrpc.model.RpcResponse;
import org.xq.xqrpc.protocol.ProtocolMessage;
import org.xq.xqrpc.protocol.ProtocolMessageDecoder;
import org.xq.xqrpc.protocol.ProtocolMessageEncoder;
import org.xq.xqrpc.protocol.utils.ProtocolMessageTypeEnum;
import org.xq.xqrpc.registry.LocalRegistry;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 请求处理器 - 服务提供者
 */
public class TcpServerHandler implements Handler<NetSocket> {


    @Override
    public void handle(NetSocket netSocket) {
        // 处理连接
        TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
            // 接受请求, 解码
            ProtocolMessage<RpcRequest> requestProtocolMessage;
            try{
                requestProtocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            }catch (IOException ex){
                throw new RuntimeException("[TcpServerHandler]: protocol decode failed");
            }
            RpcRequest rpcRequest = requestProtocolMessage.getBody();

            // 处理请求
            RpcResponse rpcResponse = new RpcResponse();
            try{
                // 通过反射进行调用
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());

                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            }catch (Exception e){
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }

            // 发送响应进行编码
            ProtocolMessage.Header header = requestProtocolMessage.getHeader();
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
            try{
                Buffer encodeBuffer = ProtocolMessageEncoder.encode(responseProtocolMessage);
                netSocket.write(encodeBuffer);
            }catch (IOException e){
                throw new RuntimeException("[TcpServerHandler]: protocol decode failed");
            }
        });
        netSocket.handler(bufferHandlerWrapper);
    }
}
