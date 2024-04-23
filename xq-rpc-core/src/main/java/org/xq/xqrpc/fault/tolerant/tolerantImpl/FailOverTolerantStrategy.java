package org.xq.xqrpc.fault.tolerant.tolerantImpl;

import org.xq.xqrpc.fault.tolerant.TolerantStrategy;
import org.xq.xqrpc.model.RpcResponse;

import java.util.Map;

/**
 * 故障转移到其它的节点
 */
public class FailOverTolerantStrategy implements TolerantStrategy {
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        return null;
    }
}
