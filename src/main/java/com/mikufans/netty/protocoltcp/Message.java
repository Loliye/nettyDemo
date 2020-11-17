package com.mikufans.netty.protocoltcp;

import lombok.Data;

@Data
public class Message
{
    private int len;
    private byte content[];
}
