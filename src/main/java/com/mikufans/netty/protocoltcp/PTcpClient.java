package com.mikufans.netty.protocoltcp;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;

public class PTcpClient
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
                        pipeline.addLast(new MsgDecoder());
                        pipeline.addLast(new MsgEncoder());
                        pipeline.addLast(new PTcpClientHandler());
                    }
                });

        ChannelFuture channelFuture = bootstrap.connect("localhost", 8080).sync();
        channelFuture.channel().closeFuture().sync();
    }
}
class PTcpClientHandler extends SimpleChannelInboundHandler<Message>
{
    private int count;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception
    {
        int len = msg.getLen();
        byte[] content = msg.getContent();

        System.out.println("客户端接收到消息如下");
        System.out.println("长度=" + len);
        System.out.println("内容=" + new String(content, Charset.forName("utf-8")));

        System.out.println("客户端接收消息数量=" + (++this.count));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        //发送数据
        for(int i = 0; i< 5; i++) {
            String mes = " hello world "+i ;
            byte[] content = mes.getBytes(CharsetUtil.UTF_8);
            int length = mes.getBytes(CharsetUtil.UTF_8).length;

            //创建协议包对象
            Message message = new Message();
            message.setLen(length);
            message.setContent(content);
            ctx.writeAndFlush(message);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        cause.printStackTrace();
        ctx.close();
    }
}
