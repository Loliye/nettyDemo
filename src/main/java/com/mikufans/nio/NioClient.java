package com.mikufans.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioClient
{
    public static void main(String[] args) throws IOException
    {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        InetSocketAddress address=new InetSocketAddress("127.0.0.1",6666);
        if(!socketChannel.connect(address))
        {
            while(!socketChannel.finishConnect())
                System.out.println("因为连接需要时间，客户端不会阻塞，可以做其它工作..");
        }

        String string = "hello world";
        ByteBuffer byteBuffer=ByteBuffer.wrap(string.getBytes());
        socketChannel.write(byteBuffer);
        System.in.read();
    }
}
