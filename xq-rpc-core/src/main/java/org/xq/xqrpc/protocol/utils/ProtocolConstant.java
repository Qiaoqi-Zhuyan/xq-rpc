package org.xq.xqrpc.protocol.utils;

/**
 * 协议常量 - 自定义协议相关信息
 */
public interface ProtocolConstant {

    /**
     * 消息头长度
     */
    int MESSAGE_HEADER_LENGTH = 17;

    /**
     * 协议magic
     */
    byte PROTOCOL_MAGIC = 0x1;

    /**
     * 消息版本号
     */
    byte PROTOCOL_VERSION = 0x1;
}
