package com.example.examplespringbootprovider.provider;

import org.springframework.stereotype.Service;
import org.xq.annotation.RpcService;
import org.xq.common.model.User;
import org.xq.common.service.UserService;

/**
 * 用户服务实现类
 */
@Service
@RpcService
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
            return user;
    }
}
