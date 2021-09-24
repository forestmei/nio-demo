import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * TODO
 *
 * @author <a href="mailto:meilinsenchn@gmail.com">meils</a>
 * @date 2021/9/23 7:01 上午
 * @since
 */
public class NIOClient extends Thread{

    private int idx = 0;
    private ProtocolHandler protocolHandler = new ProtocolHandler();

    @Override
    public void run() {
        try(Selector selector = Selector.open();
            SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 8888));
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            while (true) {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    process(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void process(SelectionKey key) throws IOException {
        if (key.isConnectable()) {
            doConnect(key);
        }
        if (key.isReadable()){
            read(key);
        }
        if(key.isWritable()){
            write(key);
        }
    }

    private void doConnect(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        if (socketChannel.isConnectionPending()) {
            socketChannel.finishConnect();
        }
        socketChannel.configureBlocking(false);
        socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    private void write(SelectionKey selectionKey) throws IOException {
        if(protocolHandler.sendCount++ < Constant.MEET_COUNT){
            protocolHandler.doWrite(selectionKey, new Message(idx++, Constant.Z_1));
        }else {
            selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
        }
    }

    private void read(SelectionKey selectionKey) throws IOException {
        List<Message> messageList = protocolHandler.doRead(selectionKey);
        for(Message message : messageList){
            switch (message.getMessage()){
                case Constant.L_2:
                    protocolHandler.doWrite(selectionKey, new Message(message.getStreamId(), Constant.Z_2));
                    break;
                case Constant.L_3:
                    protocolHandler.doWrite(selectionKey, new Message(message.getStreamId(), Constant.Z_3));
                    break;
                default:
                    break;
            }
        }
    }

    public static void main(String[] args) {
        new NIOClient().start();
    }
}
