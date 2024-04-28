package org.xq.xqrpc.serializer.SerializerImpl;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.xq.xqrpc.serializer.Serializer;

import java.io.IOException;

public class ProtostuffSerializer implements Serializer {

    /**
     * 避免每次序列化的时候重复申请buffer空间
     */
    private static final LinkedBuffer LINKED_BUFFER = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);


    @Override
    public <T> byte[] serialize(T object) throws IOException {
        Class<?> clazz = object.getClass();
        Schema schema = RuntimeSchema.getSchema(clazz);
        byte[] bytes;
        try{
            bytes = ProtostuffIOUtil.toByteArray(object, schema, LINKED_BUFFER);
        } finally {
            LINKED_BUFFER.clear();
        }
        return bytes;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws IOException {
        Schema<T> schema = RuntimeSchema.getSchema(type);
        T obj = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        return obj;
    }
}
