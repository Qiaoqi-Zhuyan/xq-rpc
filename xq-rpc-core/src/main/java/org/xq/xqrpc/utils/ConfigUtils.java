package org.xq.xqrpc.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import cn.hutool.setting.yaml.YamlUtil;
import org.xq.xqrpc.config.RpcServiceConfig;

import java.util.Map;

/**
 * 配置工具类
 */
public class ConfigUtils {

    /**
     * 加载配置对象
     * @param tClass
     * @param prefix
     * @return
     * @param <T>
     */
    public static <T> T loadConfig(String configFile, Class<T> tClass, String prefix){

        if(configFile.equals("properties"))
            return loadPropertiesConfig(tClass, prefix, "");
        if(configFile.equals("yaml"))
            return loadYamlConfig(tClass, prefix, "");
        else
            return loadYmlConfig(tClass, prefix, "");
    }

    /**
     * 加载配置对象, 支持区分环境
     * @param tClass
     * @param prefix
     * @param environment
     * @return
     * @param <T>
     */
    public static <T> T loadPropertiesConfig(Class<T> tClass, String prefix, String environment){
        StringBuilder configFileBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)){
            configFileBuilder.append("-").append(environment);
        }
        configFileBuilder.append(".properties");
        Props props = new Props(configFileBuilder.toString());
        props.autoLoad(true);
        return props.toBean(tClass, prefix);
    }


    /**
     * 加载yaml配置变量
     * @param tClass
     * @param prefix
     * @param environment
     * @return
     * @param <T>
     */
    public static <T> T loadYamlConfig(Class<T> tClass, String prefix, String environment){
        StringBuilder configFileBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)){
            configFileBuilder.append("-").append(environment);
        }
        configFileBuilder.append(".yaml");
        T config = null;
        Map<String, Object> map = YamlUtil.loadByPath(configFileBuilder.toString());
        if (map.get(prefix) != null){
            config = (T) BeanUtil.toBean(map.get(prefix), tClass);
        }
        return config;
    }



    /**
     * 加载yml配置变量
     * @param tClass
     * @param prefix
     * @param environment
     * @return
     * @param <T>
     */
    public static <T> T loadYmlConfig(Class<T> tClass, String prefix, String environment){
        StringBuilder configFileBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)){
            configFileBuilder.append("-").append(environment);
        }
        configFileBuilder.append(".yml");
        T config = null;
        Map<String, Object> map = YamlUtil.loadByPath(configFileBuilder.toString());
        if (map.get(prefix) != null){
            config = (T) BeanUtil.toBean(map.get(prefix), tClass);
        }
        return config;
    }



}
