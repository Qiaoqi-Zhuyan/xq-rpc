package org.xq.xqrpc.spi;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI加载器, 支持键值对映射
 * 读取配置并加载实现类
 * 1. 用map来存储已经加载的配置类
 * 2. 扫描指定路径, 读取每个配置文件, 获取到实现类信息存入map
 * 3. 定义获取实例的方法, 根据用户传入的接口和键名, 从map中找到对应的实现类
 * 通过反射获取到实现类对象, 维护一个对象实例缓存,创建一个的对象从缓存中读取
 */
@Slf4j
public class SpiLoader {

    /**
     * 存储已加载的类: 接口名 -> (key -> 实现类)
     * concurrentHashMap线程安全
     */
    private static final Map<String, Map<String, Class<?>>> loaderMap = new ConcurrentHashMap<>();

    /**
     * 对象实例缓存 类路径 -> 对象实例
     * 单例模式
     */
    private static final Map<String, Object> instanceCache = new ConcurrentHashMap<>();

    /**
     * 系统SPI目录
     */
    private static final String RPC_SPI_DIR = "META-INF/rpc/";


}
