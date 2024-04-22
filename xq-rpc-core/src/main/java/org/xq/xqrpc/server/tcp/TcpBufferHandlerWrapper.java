package org.xq.xqrpc.server.tcp;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;
import org.xq.xqrpc.protocol.utils.ProtocolConstant;


/**
 * 封装半包粘包处理器
 *
 * 使用recordParser 对原有buffer处理能力进行增强
 */
public class TcpBufferHandlerWrapper implements Handler<Buffer> {

    private final RecordParser recordParser;

    /**
     *
     * @param bufferHandler
     */
    public TcpBufferHandlerWrapper(Handler<Buffer> bufferHandler){
        recordParser = initRecordParser(bufferHandler);
    }

    /**
     * 初始化recordParser
     * @param bufferHandler
     * @return
     */
    private RecordParser initRecordParser(Handler<Buffer> bufferHandler){
        RecordParser parser = RecordParser.newFixed(ProtocolConstant.MESSAGE_HEADER_LENGTH);
        parser.setOutput(new Handler<Buffer>() {
            int size = -1;
            Buffer resultBuffer = Buffer.buffer();

            // 读两次, 一次读头消息, 一次读体消息, 最后拼接成完整消息
            @Override
            public void handle(Buffer buffer) {
                if (size == -1){
                    // 读取消息体长度
                    size = buffer.getInt(13);
                    parser.fixedSizeMode(size);
                    // 写入头消息到结果
                    resultBuffer.appendBuffer(buffer);
                }else{
                    // 写入体消息到结果
                    resultBuffer.appendBuffer(buffer);
                    bufferHandler.handle(resultBuffer);
                    parser.fixedSizeMode(ProtocolConstant.MESSAGE_HEADER_LENGTH);
                    size = -1;
                    resultBuffer = Buffer.buffer();
                }
            }
        });
        return parser;
    }

    @Override
    public void handle(Buffer buffer) {
        recordParser.handle(buffer);
    }
}
