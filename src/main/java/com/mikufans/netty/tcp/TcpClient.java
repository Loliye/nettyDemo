package com.mikufans.netty.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.nio.ByteBuffer;

public class TcpClient
{
    public static void main(String[] args) throws InterruptedException
    {
        EventLoopGroup group=new NioEventLoopGroup();

        Bootstrap bootstrap=new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception
                    {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new TcpClientHandler());
                    }
                });

        ChannelFuture channelFuture = bootstrap.connect("localhost", 8080).sync();
        channelFuture.channel().closeFuture().sync();
    }
}
class TcpClientHandler extends SimpleChannelInboundHandler<ByteBuf>
{
    private int count;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception
    {
        byte buffer[]=new byte[msg.readableBytes()];
        msg.readBytes(buffer);

        String message=new String(buffer,CharsetUtil.UTF_8);
        System.out.println("客户端接收到消息=" + message);
        System.out.println("客户端接收消息数量=" + (++this.count));

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        //发送数据
        for(int i=0;i<10;i++)
        {
            ByteBuf buf = Unpooled.copiedBuffer("hello world "+i, CharsetUtil.UTF_8);
            ctx.writeAndFlush(buf);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        cause.printStackTrace();
        ctx.close();
    }
}
