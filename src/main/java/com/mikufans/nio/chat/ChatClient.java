package com.mikufans.nio.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

public class ChatClient
{
    private final String HOST = "127.0.0.1";
    private final int PORT = 6666;
    private Selector selector;
    private SocketChannel socketChannel;
    private String username;

    public ChatClient() throws IOException
    {
        selector = Selector.open();

        socketChannel = SocketChannel.open(new InetSocketAddress(HOST, PORT));
//        socketChannel.bind(new InetSocketAddress(HOST, PORT));
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        username = socketChannel.getLocalAddress().toString().substring(1);
        System.out.println(username + " is ok...");
    }

    public void sendMsg(String msg) throws IOException
    {
        msg = username + " 说:" + msg;
        socketChannel.write(ByteBuffer.wrap(msg.getBytes()));
    }

    public void acceptMsg() throws IOException
    {
        int readChannles = selector.select();
        if (readChannles > 0)
        {
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext())
            {
                SelectionKey key = iterator.next();
                if (key.isReadable())
                {
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    channel.read(byteBuffer);
                    String msg = new String(byteBuffer.array());
                    System.out.println(msg.trim());
                }
            }
            iterator.remove();
        } else
        {
        }

    }

    public static void main(String[] args) throws IOException
    {
        ChatClient client=new ChatClient();

        new Thread(()->{
            while(true)
            {
                try
                {
                    client.acceptMsg();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                try
                {
                    Thread.sleep(2000);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();

        Scanner scanner=new Scanner(System.in);
        while(scanner.hasNextLine())
        {
            String msg=scanner.nextLine();
            client.sendMsg(msg);
        }
    }
}
