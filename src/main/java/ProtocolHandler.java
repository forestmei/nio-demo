import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 通信协议，一个ProtocolHandler对应一个SocketChannel
 *
 * @author <a href="mailto:meilinsenchn@gmail.com">meils</a>
 * @date 2021/9/24 6:50 上午
 * @since
 */
public class ProtocolHandler {

    public int sendCount = 0;
    private static final int DEFAULT_BUF_SIZE = 1024;
    private static final int MIN_BUF_SIZE = 256;
    private static final int MAX_BUF_SIZE = 2048;
    private final ByteBuffer writeBuffer;
    private final ByteBuffer readBuffer;

    ProtocolHandler(){
        this(0);
    }

    ProtocolHandler(int bufSize){
        bufSize = bufSize == 0 ? DEFAULT_BUF_SIZE : bufSize;
        if(bufSize > MAX_BUF_SIZE){
            bufSize = MAX_BUF_SIZE;
        }else if(bufSize < MIN_BUF_SIZE){
            bufSize = MIN_BUF_SIZE;
        }
        writeBuffer = ByteBuffer.allocateDirect(bufSize);
        readBuffer = ByteBuffer.allocateDirect(bufSize);
    }

    public void doWrite(SelectionKey selectionKey, Message message) throws IOException {
        System.out.println("发送消息：" + message.getMessage());
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        writeBuffer.clear();
        byte[] content = message.getMessage().getBytes(StandardCharsets.UTF_8);
        writeBuffer.putInt(4 + content.length);
        writeBuffer.putInt(message.getStreamId());
        writeBuffer.put(content);
        writeBuffer.flip();
        int limit = writeBuffer.limit();
        int read = 0;
        while(read != limit){
            writeBuffer.position(read);
            read += socketChannel.write(writeBuffer);
        }
    }

    public List<Message> doRead(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        socketChannel.read(readBuffer);
        readBuffer.flip();
        List<Message> messageList = new ArrayList<>();
        while (readBuffer.limit() - readBuffer.position() > 4){
            int length = readBuffer.getInt();
            if(readBuffer.limit() - readBuffer.position() < length){
                readBuffer.position(readBuffer.position() - 4);
                break;
            }
            int streamId = readBuffer.getInt();
            byte[] content = new byte[length - 4];
            readBuffer.get(content);
            String contentStr = new String(content, StandardCharsets.UTF_8);
            Message message = new Message();
            message.setStreamId(streamId);
            message.setMessage(contentStr);
            messageList.add(message);
            System.out.println("接收消息：" + contentStr);
        }
        readBuffer.compact();
        return messageList;
    }
}
