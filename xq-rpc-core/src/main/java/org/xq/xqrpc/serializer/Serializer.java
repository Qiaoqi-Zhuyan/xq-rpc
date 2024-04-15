package org.xq.xqrpc.serializer;

import java.io.IOException;

/**
 * 序列化器接口
 *
 */
public interface Serializer {

    /**
     * 序列化, 将java对象转化为字节数组
     * @param object
     * @return
     * @param <T>
     * @throws IOException
     */
    <T> byte[] serialize(T object) throws IOException;

    /**
     * 反序列化, 字节数组转化为java对象
     * @param bytes
     * @param type
     * @return
     * @param <T>
     * @throws IOException
     */
    <T> T deserialize(byte[] bytes, Class<T> type) throws IOException;

}
