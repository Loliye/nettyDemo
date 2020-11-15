package com.mikufans.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class NioServer
{
    public static void main(String[] args) throws IOException
    {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        Selector selector = Selector.open();
        serverSocketChannel.bind(new InetSocketAddress("127.0.0.1", 6666));
        serverSocketChannel.configureBlocking(false);
        //绑定 关心的事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("注册后的selectionkey 数量=" + selector.keys().size()); // 1

        //循环处理客户端请求
        while (true)
        {
            if (selector.select(1000) == 0)
            {
                System.out.println("1s内无客户端请求！");
                continue;
            }

            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            System.out.println("selectionKeys 数量 = " + selectionKeys.size());

            //处理请求
            for (SelectionKey key : selectionKeys)
            {
                if (key.isAcceptable())
                {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    System.out.println("客户端连接成功 生成了一个 socketChannel " + socketChannel.hashCode());
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                    System.out.println("客户端连接后 ，注册的selectionkey 数量=" + selector.keys().size()); //2,3,4..
                }
                if(key.isReadable())
                {
                    SocketChannel socketChannel= (SocketChannel) key.channel();
                    ByteBuffer byteBuffer= (ByteBuffer) key.attachment();
                    socketChannel.read(byteBuffer);
                    System.out.println("form 客户端 " + new String(byteBuffer.array()));
                }

                selectionKeys.remove(key);
            }
        }
    }
}
