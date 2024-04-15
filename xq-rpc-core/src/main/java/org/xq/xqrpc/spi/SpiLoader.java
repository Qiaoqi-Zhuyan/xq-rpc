package org.xq.xqrpc.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.xq.xqrpc.serializer.Serializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
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

    /**
     * 扫描路径
     */
    private static final String[] SCAN_DIR = new String[]{
            RPC_SPI_DIR
    };

    /**
     * 动态加载的类列表
     */
    private static final List<Class<?>> LOAD_CLASS_LIST = Arrays.asList(Serializer.class);

    /**
     * 加载类
     * @param loadClass
     * @return
     */
    public static Map<String, Class<?>> load(Class<?> loadClass){
        log.info("[spiLoader]: load spi type: {}", loadClass.getName());
        Map<String, Class<?>> keyClassMap = new HashMap<>();
        // 扫描路径
        for (String scanDir : SCAN_DIR){
            List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName());
            // 读取每个资源文件
            for (URL resource : resources){
                try{
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null){
                        String[] strArray = line.split("=");
                        if (strArray.length > 1){
                            String key = strArray[0];
                            String className = strArray[1];
                            // forName()方法获得与字符串对应的Class对象
                            keyClassMap.put(key, Class.forName(className));
                        }
                    }
                }catch (Exception e){
                    log.info("[spiLoader]: spi resource load error", e);
                    e.printStackTrace();
                }
            }
        }
        loaderMap.put(loadClass.getName(), keyClassMap);
        return keyClassMap;
    }


    /**
     * 加载所有类
     */
    public static void loadAll(){
        log.info("[spiLoader] load all spi");
        for (Class<?> aClass : LOAD_CLASS_LIST){
            load(aClass);
        }
    }

    /**
     * 获取接口实例
     * @param tClass
     * @param key
     * @return
     * @param <T>
     */
    public static <T> T getInstance(Class<?> tClass, String key){
        String tClassName = tClass.getName();
        Map<String, Class<?>> keyClassMap = loaderMap.get(tClassName);
        if(keyClassMap == null){
            throw new RuntimeException(String.format("[spiLoader] fail to load %s", tClassName));
        }
        if (!keyClassMap.containsKey(key)){
            throw new RuntimeException(String.format("[spiLoader] interfaces \"%s\" doesn't have key \"%s\"", tClassName, key));
        }
        // 获取到要加载的实现类
        Class<?> implClass = keyClassMap.get(key);
        // 从实例缓存中加载指定类型的实例
        String implClassName = implClass.getName();
        if (!instanceCache.containsKey(implClassName)){
            try{
                instanceCache.put(implClassName, implClass.newInstance());
            }catch (InstantiationException | IllegalAccessException e){
                String errorMsg = String.format("[spiLoader]: class instantiation failed");
                throw new RuntimeException(errorMsg, e);
            }
        }

        return (T) instanceCache.get(implClassName);
    }
}
