package com.mikufans.netty.heart;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class MyServer
{
    public static void main(String[] args)
    {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        try
        {
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>()
                    {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception
                        {
                            //加入一个netty 提供 IdleStateHandler

                            //1. IdleStateHandler 是netty 提供的处理空闲状态的处理器
                            //2. long readerIdleTime : 表示多长时间没有读, 就会发送一个心跳检测包检测是否连接
                            //3. long writerIdleTime : 表示多长时间没有写, 就会发送一个心跳检测包检测是否连接
                            //4. long allIdleTime : 表示多长时间没有读写, 就会发送一个心跳检测包检测是否连接
                            //
                            //当 IdleStateEvent 触发后 , 就会传递给管道 的下一个handler去处理
                            //通过调用(触发)下一个handler 的 userEventTriggered , 在该方法中去处理 IdleStateEvent(读空闲，写空闲，读写空闲)

                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IdleStateHandler(7000,7000,10, TimeUnit.SECONDS));
                            pipeline.addLast(new MyServerHandler());
                        }
                    });
            ChannelFuture channelFuture=bootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

class MyServerHandler extends ChannelInboundHandlerAdapter
{
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
    {
        if(evt instanceof IdleStateEvent)
        {
            IdleStateEvent event= (IdleStateEvent) evt;
            String type="";
            switch (event.state())
            {
                case READER_IDLE:
                    type = "读空闲";
                    break;
                case WRITER_IDLE:
                    type="写空闲";
                    break;
                case ALL_IDLE:
                    type="读写空闲";
                    break;
            }
            System.out.println(ctx.channel().remoteAddress() + "--超时时间--" + type);
            System.out.println("服务器做相应处理..");
        }


    }
}
