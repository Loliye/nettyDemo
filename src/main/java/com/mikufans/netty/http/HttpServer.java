package com.mikufans.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.net.URI;

public class HttpServer
{
    public static void main(String[] args)
    {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try
        {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new HttpServerInitializer());
            ChannelFuture channelFuture = bootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e)
        {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        System.out.println("hhh");
    }
}


class HttpHandler extends SimpleChannelInboundHandler<HttpObject>
{

    /**
     * 1. SimpleChannelInboundHandler 是 ChannelInboundHandlerAdapter
     * 2. HttpObject 客户端和服务器端相互通讯的数据被封装成 HttpObject
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception
    {
        System.out.println("对应的channel=" + ctx.channel() + " pipeline=" + ctx
                .pipeline() + " 通过pipeline获取channel" + ctx.pipeline().channel());
        System.out.println("当前ctx的handler=" + ctx.handler());

        if (msg instanceof HttpRequest)
        {
            System.out.println("ctx 类型=" + ctx.getClass());

            System.out.println("pipeline hashcode" + ctx.pipeline().hashCode() + " TestHttpServerHandler hash=" + this.hashCode());

            System.out.println("msg 类型=" + msg.getClass());
            System.out.println("客户端地址" + ctx.channel().remoteAddress());

            HttpRequest httpRequest = (HttpRequest) msg;
            URI uri = new URI(httpRequest.uri());
            if ("/favicon.ico".equals(uri.getPath()))
            {
                System.out.println("请求了 favicon.ico图标, 不做响应");
                return;
            }
            //http 相应客户端
            ByteBuf content = Unpooled.copiedBuffer("服务器接到了请求：" + uri.getPath(), CharsetUtil.UTF_8);

            //response 返回体
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
            response.headers().set(HttpHeaderNames.CONTENT_ENCODING,"utf-8");

            ctx.writeAndFlush(response);

        }
    }
}

class HttpServerInitializer extends ChannelInitializer<SocketChannel>
{
    //添加处理器
    @Override
    protected void initChannel(SocketChannel ch) throws Exception
    {
        ChannelPipeline pipeline = ch.pipeline();
        //加入一个netty 提供的httpServerCodec codec =>[coder - decoder]
        //HttpServerCodec 说明
        //1. HttpServerCodec 是netty 提供的处理http的 编-解码器
        pipeline.addLast("MyHttpServerCodec", new HttpServerCodec());
        pipeline.addLast("responseCoder",new HttpResponseDecoder());

        pipeline.addLast("MyTestHttpServerHandler", new HttpHandler());
        //        pipeline.addLast("compressor", new HttpContentCompressor());
        System.out.println("init....");
    }
}
