import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * TODO
 *
 * @author <a href="mailto:meilinsenchn@gmail.com">meils</a>
 * @date 2021/9/22 4:38 下午
 * @since
 */
public class NIOServer extends Thread{

    private int idx = 0;

    @Override
    public void run() {
        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()){
            serverSocketChannel.bind(new InetSocketAddress(InetAddress.getLocalHost(), 8888));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (true){
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();
                    process(selectionKey);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void process(SelectionKey selectionKey) throws IOException {
        if(selectionKey.isAcceptable()){
            doAccept(selectionKey);
        }
        if (selectionKey.isReadable()){
            read(selectionKey);
        }
        if(selectionKey.isWritable()){
            write(selectionKey);
        }
    }

    private void write(SelectionKey selectionKey) throws IOException {
        ProtocolHandler serverHandle = (ProtocolHandler) selectionKey.attachment();
        if(serverHandle.sendCount++ < Constant.MEET_COUNT){
            serverHandle.doWrite(selectionKey, new Message(idx++, Constant.L_2));
            serverHandle.doWrite(selectionKey, new Message(idx++, Constant.L_3));
        }else {
            selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
        }
    }


    private void read(SelectionKey selectionKey) throws IOException {
        ProtocolHandler serverHandle = (ProtocolHandler) selectionKey.attachment();
        List<Message> messageList = serverHandle.doRead(selectionKey);
        for(Message message : messageList){
            switch (message.getMessage()){
                case Constant.Z_1:
                    serverHandle.doWrite(selectionKey, new Message(message.getStreamId(), Constant.L_1));
                    break;
                default:
                    break;
            }
        }
    }

    private void doAccept(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        ProtocolHandler serverHandle = new ProtocolHandler();
        socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE, serverHandle);
    }

    public static void main(String[] args) {
        new NIOServer().start();
    }

}
