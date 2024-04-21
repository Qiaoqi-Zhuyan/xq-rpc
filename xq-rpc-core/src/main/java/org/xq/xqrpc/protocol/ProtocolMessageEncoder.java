package org.xq.xqrpc.protocol;

import io.vertx.core.buffer.Buffer;
import org.xq.xqrpc.protocol.utils.ProtocolMessageSerializerEnum;
import org.xq.xqrpc.serializer.Serializer;
import org.xq.xqrpc.serializer.SerializerFactory;

import java.io.IOException;

/**
 * 消息编码器
 */
public class ProtocolMessageEncoder {

    /**
     * 编码, 依次向buffer写入消息对象里的字段
     * @param protocolMessage
     * @return
     * @throws IOException
     */
    public static Buffer encode(ProtocolMessage<?> protocolMessage) throws IOException{
        if (protocolMessage == null || protocolMessage.getHeader() == null)
            return Buffer.buffer();
        ProtocolMessage.Header header = protocolMessage.getHeader();
        // 依次向buffer中写入数据
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(header.getMagic());
        buffer.appendByte(header.getVersion());
        buffer.appendByte(header.getSerializer());
        buffer.appendByte(header.getType());
        buffer.appendByte(header.getStatus());
        buffer.appendLong(header.getRequestId());
        // 序列化body
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null)
            throw new RuntimeException("[ProtocolMessageEncoder]: serializer doesn't exist");
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        byte[] bodyBytes = serializer.serialize(protocolMessage.getBody());
        buffer.appendInt(bodyBytes.length);
        buffer.appendBytes(bodyBytes);

        return buffer;
    }
}
