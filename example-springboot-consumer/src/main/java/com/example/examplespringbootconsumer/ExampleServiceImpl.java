package com.example.examplespringbootconsumer;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.xq.annotation.RpcReference;
import org.xq.common.model.User;
import org.xq.common.service.UserService;

import javax.annotation.Resource;

@Service
public class ExampleServiceImpl {

    @RpcReference
    private UserService userService;

    @Bean
    public void consumer(){
        for (int i = 0; i < 5; i++){
            User user = userService.getUser(new User("xiaoqi user"));
            System.out.println("[EasyConsumer]: " + user + " " + (i + 1));
        }
    }
}
