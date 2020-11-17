package com.mikufans.netty.protocoltcp;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;
import java.util.UUID;

//处理tcp 粘包
public class PTcpServer
{
    public static void main(String[] args) throws InterruptedException
    {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>()
                {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception
                    {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new MsgDecoder());
                        pipeline.addLast(new MsgEncoder());
                        pipeline.addLast(new PTcpServerHandler());
                    }
                });

        ChannelFuture channelFuture = bootstrap.bind(8080).sync();
        channelFuture.channel().closeFuture().sync();
    }
}

class PTcpServerHandler extends SimpleChannelInboundHandler<Message>
{
    private int count;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception
    {
        int len = msg.getLen();
        byte content[] = msg.getContent();

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("服务器接收到信息如下");
        System.out.println("长度=" + len);
        System.out.println("内容=" + new String(content, CharsetUtil.UTF_8));
        System.out.println("服务器接收到消息包数量=" + (++this.count));

        String respContent = UUID.randomUUID().toString();
        int respLen = respContent.getBytes(CharsetUtil.UTF_8).length;
        byte respData[] = respContent.getBytes(CharsetUtil.UTF_8);

        Message message=new Message();
        message.setLen(respLen);
        message.setContent(respData);
        ctx.writeAndFlush(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        ctx.close();
    }
}
