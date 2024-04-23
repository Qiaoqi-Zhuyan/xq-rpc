package org.xq.xqrpc.fault.tolerant;

import org.xq.xqrpc.spi.SpiLoader;

public class TolerantStrategyFactory {

    /**
     * 加载容错方法
     */
    private static class TolerantStrategyHolder{
        private static TolerantStrategy loadTolerantStrategy(String key){
            SpiLoader.load(TolerantStrategy.class);
            return SpiLoader.getInstance(TolerantStrategy.class, key);
        }
    }

    /**
     * 默认容错方式
     */
    public static final String DEFAULT_TOLERANT_STRATEGY = TolerantStrategyKeys.FAIL_SAFE;

    /**
     * 获取实例
     * @param key
     * @return
     */
    public static TolerantStrategy getInstance(String key){
        return TolerantStrategyHolder.loadTolerantStrategy(key);
    }

}
