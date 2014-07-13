package com.joey.common;

import java.nio.ByteBuffer;

/**
 * Created by joey on 2014-7-2.
 */
public class ProtoHeader {

    public static final int HEADER_BYTE_LENGTH = 18;

    public int		len;	// 协议总长度
    public int		seq;	// 序列号，用于一一对应
    public int		hash;	// 用于分库分表的数字
    public short	cmd;	// 命令号：1. 用于选择正确的库； 2. 用于定位到正确的处理函数。
    public short	ret;	// 错误码
    public short	hlen;	// protobuf格式的标准包头


    public static byte[] packHeader(ProtoHeader header) {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_BYTE_LENGTH);
        buffer.putInt(header.len);
        buffer.putInt(header.seq);
        buffer.putInt(header.hash);
        buffer.putShort(header.cmd);
        buffer.putShort(header.ret);
        buffer.putShort(header.hlen);

        return buffer.array();
    }

    public static ProtoHeader parseHeader(ByteBuffer buffer) {
        ProtoHeader header = new ProtoHeader();

        header.len = buffer.getInt();
        header.seq = buffer.getInt();
        header.hash = buffer.getInt();
        header.cmd = buffer.getShort();
        header.ret = buffer.getShort();
        header.hlen = buffer.getShort();

        return header;
    }

    @Override
    public String toString()
    {
        return "[ProtoHeader][len = " + len + ", seq = " + seq + ", hash = " + hash + ", cmd = " + cmd + ", ret = " + ret + ", hlen = " + hlen
                + "]";
    }
}
