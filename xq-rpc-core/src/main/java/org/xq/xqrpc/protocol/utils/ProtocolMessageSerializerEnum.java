package org.xq.xqrpc.protocol.utils;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 协议消息的序列化器
 */
@Getter
public enum ProtocolMessageSerializerEnum {

    FastJSON(0, "fastjson"),
    Hessian(1, "hessian"),
    JSON(2, "json"),
    KRYO(3, "kryo"),
    PROTOSTUFF(4, "protostuff");


    private final int key;

    private final String value;

    ProtocolMessageSerializerEnum(int key, String value){
        this.key = key;
        this.value = value;
    }

    /**
     * 获取键值对
     *
     * note: Collectors.toList()是一个收集器（Collector），它负责将流中的元素收集到一个列表中
     * Arrays.stream(values()): 这部分将枚举类型的数组转换为一个流（Stream）
     */
    public static List<String> getValues(){
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据key获取枚举类
     * @param key
     * @return
     */
    public static ProtocolMessageSerializerEnum getEnumByKey(int key){
        for (ProtocolMessageSerializerEnum anEnum : ProtocolMessageSerializerEnum.values()){
            if (anEnum.key == key)
                return anEnum;
        }
        return null;
    }

    /**
     * 根据value获取枚举类
     * @param value
     * @return
     */
    public static ProtocolMessageSerializerEnum getEnumByValue(String value){
        for (ProtocolMessageSerializerEnum anEnum : ProtocolMessageSerializerEnum.values()){
            if (anEnum.value.equals(value))
                return anEnum;
        }
        return null;
    }

}
