package com.doogga.worker;

import com.doogga.common.Pair;
import com.doogga.common.ProtoHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by joey on 14-7-2.
 */
public abstract class AbstractWorker {
    private final static Logger logger = LoggerFactory.getLogger(AbstractWorker.class);

    /**
     * pair contain selectionkey, protoheader and message body
     */
    protected Pair pair;


    protected void write(SelectionKey channelKey, Pair pair)  {
        if (channelKey == null || !channelKey.isValid()) {
            logger.error("SelectionKey is null");
            return ;
        }

        if (pair == null) {
            logger.error("header and message body container is null.");
            return;
        }

        Object obj = channelKey.attachment();
        if (! (obj instanceof ConcurrentHashMap)) {
            logger.error("The SelectionKey's attachment object must be ConcurrentHashMap.");
            return;
        }

        ConcurrentHashMap<SelectionKey, ByteBuffer> map = (ConcurrentHashMap<SelectionKey, ByteBuffer>) obj;

        ProtoHeader header = (ProtoHeader) pair.getKey();
        ByteBuffer msgBody = (ByteBuffer) pair.getValue();
        byte[] buffer = msgBody.array();

        short len = (short)buffer.length;
        byte[] lengthBytes = ProtoHeader.packHeader(header);

        ByteBuffer writeBuffer = ByteBuffer.allocate(len+lengthBytes.length);
        writeBuffer.put(lengthBytes);
        writeBuffer.put(buffer);
        writeBuffer.flip();
        if (buffer != null) {
            int bytesWritten;
            try {
                // only 1 thread can write to a channel at a time
                System.out.println("write message back to client : " + new String(buffer));
                SocketChannel channel = (SocketChannel)channelKey.channel();
                synchronized (channel) {
                    bytesWritten = channel.write(writeBuffer);
                }
                if (bytesWritten==-1) {
                    // TODO 如果一次发送多天数据，那么一条一条的响应时出现异常了，该SelectionKey就被cancel了
                    channelKey.cancel();
                    map.remove(channelKey);
                }
            } catch (IOException e) {
                // TODO 如果一次发送多天数据，那么一条一条的响应时出现异常了，该SelectionKey就被cancel了
                channelKey.cancel();
                map.remove(channelKey);
            }
        }
    }
}
