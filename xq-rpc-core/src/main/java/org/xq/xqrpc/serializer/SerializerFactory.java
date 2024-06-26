package org.xq.xqrpc.serializer;

import org.xq.xqrpc.serializer.SerializerImpl.FastJsonSerializer;
import org.xq.xqrpc.spi.SpiLoader;



/**
 * 序列化器工厂 - 获取序列化器对象
 *
 * 工厂模式 + 单例模式 实现获取可复用序列化器对象
 */
public class SerializerFactory {

    /**
     * 静态内部类加载序列化器实现实例
     */
    private static class SerializerSingleton{
        private static Serializer loadSerializer(String key){
            SpiLoader.load(Serializer.class);
            return SpiLoader.getInstance(Serializer.class, key);
        }
    }

    /**
     * 默认序列化器
     */
    private static final Serializer DEFAULT_SERIALIZER = new FastJsonSerializer();

    /**
     * 获取实例
     * @param key
     * @return
     */
    public static Serializer getInstance(String key){
        return SerializerSingleton.loadSerializer(key);
    }
}
