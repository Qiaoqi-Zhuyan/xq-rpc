package org.xq.xqrpc.server;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.xq.xqrpc.RpcApplication;
import org.xq.xqrpc.model.RpcRequest;
import org.xq.xqrpc.model.RpcResponse;
import org.xq.xqrpc.registry.LocalRegistry;
import org.xq.xqrpc.serializer.FastJsonSerializer.FastJsonSerializer;
import org.xq.xqrpc.serializer.JdkSerializer.JdkSerializer;
import org.xq.xqrpc.serializer.Serializer;
import org.xq.xqrpc.serializer.SerializerFactory;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * http 请求处理
 * 1. 反序列化请求为对象, 从请求对象中获取参数
 * 2. 根据服务名称从本地注册器中获取到对应的服实现类
 * 3. 通过反射机制调用方法, 得到返回结果
 * 4. 对返回结果进行封装和序列化, 并写入响应中
 *
 */
public class HttpServerHandler implements Handler<HttpServerRequest> {

    @Override
    public void handle(HttpServerRequest httpServerRequest) {
        // 指定序列化器
//        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getConfig().getSerializer());
        final Serializer serializer = new FastJsonSerializer();
        System.out.println("Received request: " + httpServerRequest.method() + " " + httpServerRequest.uri());

        // 异步处理http请求
        httpServerRequest.bodyHandler(body ->{
            byte[] bytes = body.getBytes();
            RpcRequest rpcRequest = null;
            try{
                rpcRequest = serializer.deserialize(bytes, RpcRequest.class);
            }catch (Exception e){
                e.printStackTrace();
            }

            // 构造响应结果对象
            RpcResponse rpcResponse = new RpcResponse();
            // 如果请求为null, 直接返回
            if(rpcRequest == null){
                rpcResponse.setMessage("rpcRequest is null");
                doResponse(httpServerRequest, rpcResponse, serializer);
                return;
            }

            // 通过反射获取要调用的服务实现类
            try{
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                // 封装返回结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            }catch (Exception e){
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }

            // 响应
            doResponse(httpServerRequest, rpcResponse, serializer);
        });
    }


    /**
     * 响应
     * @param request
     * @param rpcResponse
     * @param serializer
     */
    public void doResponse(HttpServerRequest request, RpcResponse rpcResponse, Serializer serializer){
        HttpServerResponse httpServerResponse = request.response().putHeader("content-type", "application/json");
        try{
            byte[] serialized = serializer.serialize(rpcResponse);
            httpServerResponse.end(Buffer.buffer(serialized));
        }catch (IOException e){
            e.printStackTrace();
            httpServerResponse.end(Buffer.buffer());
        }
    }
}

