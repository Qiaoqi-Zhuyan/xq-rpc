package org.xq.consumer;

import org.xq.common.model.User;
import org.xq.common.service.UserService;
import org.xq.xqrpc.config.RpcServiceConfig;
import org.xq.xqrpc.constant.RpcConstant;
import org.xq.xqrpc.proxy.ServiceProxyFactory;
import org.xq.xqrpc.utils.ConfigUtils;

/**
 * 消费者示意
 */
public class ConsumerExample {
    public static void main(String[] args){
        // 可选yaml, yml和properties
//        RpcServiceConfig serviceConfig = ConfigUtils.loadConfig(RpcConstant.YAML_CONFIG_FILE, RpcServiceConfig.class, "rpc");
//        System.out.println(serviceConfig);
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        System.out.println(userService.getUser(new User("xiaoqi user")));
    }
}
