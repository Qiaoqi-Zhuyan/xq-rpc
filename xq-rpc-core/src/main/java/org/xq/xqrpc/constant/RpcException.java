package org.xq.xqrpc.constant;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * rpc 异常信息
 */
@AllArgsConstructor
public enum RpcException {

    CLIENT_CONNECT_SERVER_FAILURE("client_connect_server_fail");


    private final String errorMessage;
}

