package org.xq.xqrpc.serializer;

import org.xq.xqrpc.serializer.FastJsonSerializer.FastJsonSerializer;
import org.xq.xqrpc.serializer.JsonSerializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * 序列化器工厂 - 获取序列化器对象
 *
 * 工厂模式 + 单例模式 实现获取可复用序列化器对象
 */
public class SerializerFactory {

    /**
     * 序列化器映射
     */
    private static final Map<String, Serializer> KEY_SERIALIZER_MAP = new HashMap<String, Serializer>(){
        {
            put(SerializerKeys.JSON, new JsonSerializer());
            put(SerializerKeys.FastJson, new FastJsonSerializer());
        }
    };

    /**
     * 默认序列化器
     */
    private static final Serializer DEFAULT_SERIALIZER = KEY_SERIALIZER_MAP.get(SerializerKeys.FastJson);

    /**
     * 获取实例
     * @param key
     * @return
     */
    public static Serializer getInstance(String key){
        return KEY_SERIALIZER_MAP.getOrDefault(key, DEFAULT_SERIALIZER);
    }
}
