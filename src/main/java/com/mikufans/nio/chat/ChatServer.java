package com.mikufans.nio.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class ChatServer
{
    private final String HOST = "127.0.0.1";
    private final int PORT = 6666;
    private Selector selector;
    private ServerSocketChannel listenChannel;

    public ChatServer()
    {
        try
        {
            selector=Selector.open();
            listenChannel=ServerSocketChannel.open();
            listenChannel.bind(new InetSocketAddress(HOST,PORT));
            listenChannel.configureBlocking(false);
            listenChannel.register(selector,SelectionKey.OP_ACCEPT);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void listen() throws IOException
    {
        System.out.println("监听线程："+Thread.currentThread().getName());
        while(true)
        {
            int cnt=selector.select(1000);
            if(cnt==0) continue;

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while(iterator.hasNext())
            {
                SelectionKey key=iterator.next();
                if(key.isAcceptable())
                {
//                    SocketChannel socketChannel= (SocketChannel) key.channel();
                    SocketChannel socketChannel=listenChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector,SelectionKey.OP_READ);

                    System.out.println(socketChannel.getRemoteAddress() + " 上线 ");
                }
                if(key.isReadable())
                    readData(key);

                iterator.remove();
            }
        }
    }

    private void readData(SelectionKey key) throws IOException
    {
        SocketChannel channel=null;

        try
        {
            channel= (SocketChannel) key.channel();

            ByteBuffer buffer=ByteBuffer.allocate(1024);
            int count=channel.read(buffer);
            if(count>0)
            {
                String msg=new String(buffer.array());
                System.out.println("form 客户端: " + msg);

                sendMsgToOther(msg,channel);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            System.out.println(channel.getRemoteAddress() + " 离线了..");
            //取消注册
            key.cancel();
            //关闭通道
            channel.close();
        }
    }

    private void sendMsgToOther(String msg,SocketChannel self) throws IOException
    {
        System.out.println("服务器转发消息中...");
        System.out.println("服务器转发数据给客户端线程: " + Thread.currentThread().getName());
        for(SelectionKey key:selector.selectedKeys())
        {
            //转发给其他
            Channel targetChannel=key.channel();
            if(targetChannel instanceof  SocketChannel &&targetChannel!=self)
            {
                SocketChannel other= (SocketChannel) targetChannel;
                ByteBuffer buffer= ByteBuffer.wrap(msg.getBytes());
                other.write(buffer);
            }
        }
    }

    public static void main(String[] args) throws IOException
    {
        ChatServer server=new ChatServer();
        server.listen();
    }

}
