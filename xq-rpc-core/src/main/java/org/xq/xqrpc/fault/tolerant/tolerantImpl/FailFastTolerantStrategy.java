package org.xq.xqrpc.fault.tolerant.tolerantImpl;

import org.xq.xqrpc.fault.tolerant.TolerantStrategy;
import org.xq.xqrpc.model.RpcResponse;

import java.util.Map;

/**
 * 快速失败 - 立刻通知外层调用方
 */
public class FailFastTolerantStrategy implements TolerantStrategy {
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        throw new RuntimeException("Service Failed", e);
    }
}
