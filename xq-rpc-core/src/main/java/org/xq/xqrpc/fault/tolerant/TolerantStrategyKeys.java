package org.xq.xqrpc.fault.tolerant;

public interface TolerantStrategyKeys {

    /**
     * 降级到其它的服务
     */
    public String FAIL_BACK = "failBack";

    /**
     * 快速失败
     */
    public String FAIL_FAST = "failFast";

    /**
     * 故障转移到其它的节点
     */
    public String FAIL_OVER = "failOver";

    /**
     * 静默处理
     */
    public String FAIL_SAFE = "failSafe";

}
