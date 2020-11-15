package com.mikufans.netty.simple;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.netty.util.CharsetUtil.UTF_8;

public class NettyServer
{
    public static void main(String[] args)
    {
        //bossGroup 只是处理连接请求 , 真正的和客户端业务处理，会交给 workerGroup完成
        EventLoopGroup bossGroup =new NioEventLoopGroup(1);
        EventLoopGroup workerGroup=new NioEventLoopGroup();//默认cpu*2
        try
        {
            ServerBootstrap bootstrap=new ServerBootstrap();

            bootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)//使用的管道类型
                    .option(ChannelOption.SO_BACKLOG,128)//线程队列连接数
                    .childOption(ChannelOption.SO_KEEPALIVE,true)//保持连接状态
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        //pipeline添加处理器 两个group的
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception
                        {
                            System.out.println("客户socketchannel hashcode=" + socketChannel.hashCode());
                            //可以使用一个集合管理 SocketChannel， 再推送消息时，可以将业务加入到各个channel 对应的 NIOEventLoop 的 taskQueue 或者 scheduleTaskQueue
                            socketChannel.pipeline().addLast(new ServerHandler());
                        }
                    });

            System.out.println(".....服务器准备启动.....");
            ChannelFuture future=bootstrap.bind(6666).sync();
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception
                {
                    if (future.isSuccess()) {
                        System.out.println("监听端口 6666 成功");
                    } else {
                        System.out.println("监听端口 6666 失败");
                    }
                }
            });
            //对关闭通道进行监听
            future.channel().closeFuture().sync();
        } catch (InterruptedException e)
        {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }
}
class ServerHandler extends ChannelInboundHandlerAdapter
{
    //读取数据

    /**
     *
     * @param ctx 上下文对象, 含有 管道pipeline , 通道channel, 地址
     * @param msg 就是客户端发送的数据 默认Object
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        System.out.println("服务器读取线程 " + Thread.currentThread().getName() + " channle =" + ctx.channel());
        System.out.println("server ctx =" + ctx);
        System.out.println("看看channel 和 pipeline的关系");
        //比如这里我们有一个非常耗时长的业务-> 异步执行 -> 提交该channel 对应的
//        ctx.channel().eventLoop().execute(()->{
//            try {
//                Thread.sleep(5 * 1000);
//                ctx.writeAndFlush(Unpooled.copiedBuffer("hello, 客户端~(>^ω^<)喵2", CharsetUtil.UTF_8));
//                System.out.println("channel code=" + ctx.channel().hashCode());
//            } catch (Exception ex) {
//                System.out.println("发生异常" + ex.getMessage());
//            }
//        });

        //使用定时任务
//        ctx.channel().eventLoop().schedule(new Runnable() {
//            @Override
//            public void run() {
//
//                try {
//                    Thread.sleep(5 * 1000);
//                    ctx.writeAndFlush(Unpooled.copiedBuffer("hello, 客户端~(>^ω^<)喵4", CharsetUtil.UTF_8));
//                    System.out.println("channel code=" + ctx.channel().hashCode());
//                } catch (Exception ex) {
//                    System.out.println("发生异常" + ex.getMessage());
//                }
//            }
//        }, 5, TimeUnit.SECONDS);


        Channel channel=ctx.channel();
        ChannelPipeline pipeline=ctx.pipeline();

        //netty中  非nio中byteBuffer
        ByteBuf buf= (ByteBuf) msg;
        System.out.println("客户端发送消息是:" + buf.toString(UTF_8));
        System.out.println("客户端地址:" + channel.remoteAddress());
    }

    //数据读取完成
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception
    {
        //writeAndFlush 是 write + flush
        //将数据写入到缓存，并刷新
        //一般讲，我们对这个发送的数据进行编码
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello, 客户端~(>^ω^<)喵1", UTF_8));
    }

    //处理异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        ctx.close();
    }
}
