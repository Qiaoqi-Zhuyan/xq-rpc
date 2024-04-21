package org.xq.xqrpc.protocol;

import io.vertx.core.buffer.Buffer;
import org.xq.xqrpc.model.RpcRequest;
import org.xq.xqrpc.model.RpcResponse;
import org.xq.xqrpc.protocol.utils.ProtocolConstant;
import org.xq.xqrpc.protocol.utils.ProtocolMessageSerializerEnum;
import org.xq.xqrpc.protocol.utils.ProtocolMessageTypeEnum;
import org.xq.xqrpc.serializer.Serializer;
import org.xq.xqrpc.serializer.SerializerFactory;

import java.io.IOException;

/**
 * 消息解码器
 */
public class ProtocolMessageDecoder {

    /**
     * 依次从buffer缓冲区的指定位置读取字段构造出消息对象
     * @param buffer
     * @return
     * @throws IOException
     */
    public static ProtocolMessage<?> decode(Buffer buffer) throws IOException{
        // 依次从buffer读数据
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        byte magic = buffer.getByte(0);
        if (magic != ProtocolConstant.PROTOCOL_MAGIC)
            throw new RuntimeException("[ProtocolMessageDecoder]: illegal magic");
        header.setMagic(magic);
        header.setVersion(buffer.getByte(1));
        header.setSerializer(buffer.getByte(2));
        header.setType(buffer.getByte(3));
        header.setStatus(buffer.getByte(4));
        header.setRequestId(buffer.getLong(5));
        header.setBodyLength(Buffer.buffer().getInt(13));
        // 指定长度, 避免粘爆
        byte[] bodyBytes = buffer.getBytes(17, 17 + header.getBodyLength());
        // 解析body
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null)
            throw new RuntimeException("[ProtocolMessageDecoder]: Serializer doesn't exist");
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        ProtocolMessageTypeEnum messageTypeEnum = ProtocolMessageTypeEnum.getEnumByKey(header.getType());
        if (messageTypeEnum == null)
            throw new RuntimeException("[ProtocolMessageDecoder]: message type doesn't exist");

        switch (messageTypeEnum){
            case REQUEST:
                RpcRequest request = serializer.deserialize(bodyBytes, RpcRequest.class);
                return new ProtocolMessage<>(header, request);
            case RESPONSE:
                RpcResponse response = serializer.deserialize(bodyBytes, RpcResponse.class);
                return new ProtocolMessage<>(header, response);
            case HEART_BEAT:
            case OTHERS:
            default:
                throw new RuntimeException("[ProtocolMessageDecoder]: illegal message type");
        }

    }
}
