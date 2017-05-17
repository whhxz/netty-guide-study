package com.whh.chaptor0203;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * 多路复用器
 * 处理多个客户端的并发接入
 * Created by xuzhuo on 2017/5/16.
 */
public class MultiplexerTimeServer implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(MultiplexerTimeServer.class);

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private volatile boolean stop;

    /**
     * 初始化多路复用器,把serverSocketChannel注册到selector上
     * @param port
     */
    public MultiplexerTimeServer(int port) {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(port), 1024);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            logger.info("The time server is start in port :{}", port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStop(boolean stop){
        this.stop = stop;
    }

    @Override
    public void run() {
        while (!stop){
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                SelectionKey selectionKey;
                while (iterator.hasNext()){
                    selectionKey = iterator.next();
                    //需要移除,否则会null
                    iterator.remove();
                    try {
                        handleInput(selectionKey);
                    } catch (IOException e) {
                        if (selectionKey != null){
                            selectionKey.cancel();
                            if (selectionKey.channel() != null){
                                try {
                                    selectionKey.channel().close();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //多路复用器关闭后,所有注册在上面的channel和pipe自动关闭
        if (selector != null){
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()){
            //处理新接入的请求信息
            if (key.isAcceptable()){
                //建立连接,连接后相当于TCP已经三次握手
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel sc = ssc.accept();
                sc.configureBlocking(false);
                sc.register(selector, SelectionKey.OP_READ);
            }
            //读取客户端请求
            if (key.isReadable()){
                SocketChannel sc = (SocketChannel) key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                //read为非阻塞
                int readBytes = sc.read(readBuffer);
                //大于0,读取到了数据, 等于0,没读取到数据,正常;小于0,链路已经关闭
                if (readBytes > 0){
                    //flip反转缓冲区,在准备从缓冲区读取数据时使用
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes, "UTF-8");
                    logger.info("The time server receive order: {}", body);
                    String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body)? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()): "BAD ORDER";
                    doWrite(sc, currentTime);
                } else if (readBytes < 0){
                    key.channel();
                    sc.close();
                }
            }
        }
    }

    private void doWrite(SocketChannel sc, String response) throws IOException {
        if (response != null && response.trim().length() >0){
            byte[] bytes = response.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            //此处因为是异步非阻塞,可能出现数据未读取完全,需要使用hasRemain,暂时未实现
            writeBuffer.put(bytes);
            writeBuffer.flip();
            sc.write(writeBuffer);
        }
    }
}
