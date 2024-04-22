package org.xq.xqrpc.loadBalancer;

/**
 * 列举所有支持的负载均衡器建键名
 */
public class LoadBalancerKeys {

    public static final String ROUND_ROBIN = "roundRobin";

    public static final String RANDOM = "random";

    public static final String CONSISTENT_HASH = "consistentHash";
}
