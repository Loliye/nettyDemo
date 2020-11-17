package com.mikufans.netty.protocoltcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class MsgDecoder extends ReplayingDecoder<Void>
{
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        //数据包转化为对象
        int len=in.readInt();
        byte content[]=new byte[len];
        in.readBytes(content);

        Message message=new Message();
        message.setLen(len);
        message.setContent(content);
        out.add(message);
    }
}
