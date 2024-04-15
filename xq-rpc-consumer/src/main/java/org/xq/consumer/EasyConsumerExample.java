package org.xq.consumer;

import org.xq.common.model.User;
import org.xq.common.service.UserService;
import org.xq.xqrpc.proxy.ServiceProxyFactory;

/**
 * 简易服务消费者示例
 */
public class EasyConsumerExample {

    public static void main(String[] args){
        // 静态代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User("xiaoqi");
        User newUser = userService.getUser(user);
        if (newUser != null)
            System.out.println(newUser.getName());
        else
            System.out.println("user is null");
    }
}
