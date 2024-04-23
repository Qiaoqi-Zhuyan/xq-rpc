package org.xq.provider;

import org.xq.common.service.UserService;
import org.xq.xqrpc.bootstrap.ProviderBootstrap;
import org.xq.xqrpc.model.ServiceRegisterInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务提供者示意
 */
public class ProviderExample {
    public static void main(String[] args){
        // 提供的服务
        List<ServiceRegisterInfo<?>> serviceRegisterInfoList = new ArrayList<>();
        ServiceRegisterInfo<UserService> serviceRegisterInfo = new ServiceRegisterInfo<UserService>(UserService.class.getName(), UserServiceImpl.class);
        serviceRegisterInfoList.add(serviceRegisterInfo);

        // 服务提供者初始化
        ProviderBootstrap.init(serviceRegisterInfoList);
    }
}
