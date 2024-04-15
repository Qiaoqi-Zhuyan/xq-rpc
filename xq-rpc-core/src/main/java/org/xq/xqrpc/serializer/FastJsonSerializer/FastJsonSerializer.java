package org.xq.xqrpc.serializer.FastJsonSerializer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import org.xq.xqrpc.model.RpcRequest;
import org.xq.xqrpc.model.RpcResponse;
import org.xq.xqrpc.serializer.Serializer;

import java.io.IOException;

public class FastJsonSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) throws IOException {
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws IOException {
        T object = JSON.parseObject(bytes, type, JSONReader.Feature.SupportClassForName);
        if (object instanceof RpcRequest){
            return handleRequest((RpcRequest) object, type);
        }
        if (object instanceof RpcResponse){
            return handleResponse((RpcResponse) object, type);
        }
        return object;
    }

    /**
     * Object 原始对象会被擦除, 导致反序列化时会被作为LinkedHashMap无法转化为原始对象
     * 做一些处理
     * @param rpcRequest
     * @param type
     * @return
     * @param <T>
     * @throws IOException
     */
    private <T> T handleRequest(RpcRequest rpcRequest, Class<T> type) throws IOException{
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] args = rpcRequest.getArgs();

        // 循环处理每个参数
        for (int i = 0; i < parameterTypes.length; i++){
            Class<?> clazz = parameterTypes[i];
            // 类型不同情况下的处理
            if (! clazz.isAssignableFrom(args[i].getClass())){
                byte[] argBytes = JSONB.toBytes(args[i]);
                args[i] = JSONB.parseObject(argBytes, clazz);
            }
        }
        return type.cast(rpcRequest);
    }

    private <T> T handleResponse(RpcResponse rpcResponse, Class<T> type) throws IOException{
        // 处理响应数据
        byte[]  dataBytes = JSONB.toBytes(rpcResponse.getData());
        rpcResponse.setData(JSONB.parseObject(dataBytes, rpcResponse.getDataType()));
        return type.cast(rpcResponse);
    }
}
