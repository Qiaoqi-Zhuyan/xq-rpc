package org.xq.xqrpc.fault.tolerant.tolerantImpl;

import org.xq.xqrpc.fault.tolerant.TolerantStrategy;
import org.xq.xqrpc.model.RpcResponse;

import java.util.Map;

/**
 * 降级到其它的服务
 */
public class FailBackTolerantStrategy implements TolerantStrategy {
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        return null;
    }
}
