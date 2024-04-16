package org.xq.xqrpc.registry;

import org.checkerframework.checker.units.qual.K;
import org.xq.xqrpc.registry.EtcdRegistry.EtcdRegistry;
import org.xq.xqrpc.serializer.Serializer;
import org.xq.xqrpc.spi.SpiLoader;

/**
 * 注册中心工厂 (用于获取注册中心对象)
 */
public class RegistryFactory {

    /**
     * 加载注册中心对象
     */
    private static class RegistrySingleton{
        private static Registry loadRegistry(String key){
            SpiLoader.load(Registry.class);
            return SpiLoader.getInstance(Registry.class, key);
        }
    }

    /**
     * 默认注册中心
     */
    private static final Registry DEFAULT_REGISTRY = new EtcdRegistry();

    /**
     * 获取实例
     * @param key
     * @return
     */
    public static Registry getInstance(String key){
        return RegistrySingleton.loadRegistry(key);
    }
}
