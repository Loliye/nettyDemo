package com.mikufans.netty.protocoltcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MsgEncoder extends MessageToByteEncoder<Message>
{
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception
    {
        out.writeInt(msg.getLen());
        out.writeBytes(msg.getContent());
        System.out.println("MsgEncoder.... "+out.toString());
    }
}
