package org.xq.xqrpc.fault.tolerant;

import io.protostuff.Rpc;
import org.xq.xqrpc.model.RpcResponse;

import java.util.Map;

/**
 *容错策略
 */
public interface TolerantStrategy {

    /**
     * 容错 - Map接受上下文的信息，用于灵活地传递容器处理中需要用到的数据
     * @param context
     * @param e
     * @return
     */
    RpcResponse doTolerant(Map<String, Object> context, Exception e);

}
