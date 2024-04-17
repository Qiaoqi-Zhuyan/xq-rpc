package org.xq.xqrpc.proxy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Mock 服务代理
 */
@Slf4j
public class MockServiceProxy implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(MockServiceProxy.class);
    /**
     * 调用代理
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 根据方法的返回值类型, 生成特定的默认值对象
        Class<?> methodReturnType = method.getReturnType();
        logger.info("mock invoke{}", method.getName());
        return getDefaultObject(methodReturnType);
    }

    /**
     * 生成指定类型的默认值对象
     * @param type
     * @return
     */
    private Object getDefaultObject(Class<?> type){
        if (type.isPrimitive()){
            if (type == boolean.class){
                return false;
            } else if (type == short.class) {
                return (short) 0;
            } else if (type == int.class) {
                return 0;
            } else if (type == long.class){
                return 0L;
            } else if (type == double.class){
                return 0f;
            } else if (type == float.class){
                return 0f;
            }
        }
        return null;
    }
}

