package org.xq.consumer;

import ch.qos.logback.classic.Level;
import io.vertx.core.Verticle;
import io.vertx.core.impl.logging.LoggerFactory;
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
        for (int i = 0; i < 3; i++){
            UserService userService = ServiceProxyFactory.getProxy(UserService.class);
            User user = userService.getUser(new User("xiaoqi user"));
            System.out.println("[EasyConsumer]: "  + user);
        }
    }
}
