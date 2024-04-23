package org.xq.xqrpc.fault.tolerant.tolerantImpl;

import lombok.extern.slf4j.Slf4j;
import org.xq.xqrpc.fault.tolerant.TolerantStrategy;
import org.xq.xqrpc.model.RpcResponse;

import java.util.Map;

/**
 * 静默处理 - 遇到异常后，记录一条日志然后返回一个响应对象
 */
@Slf4j
public class FailSafeTolerantStrategy implements TolerantStrategy {
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        log.info("fail safe tolerant Strategy", e);
        return new RpcResponse();
    }
}
