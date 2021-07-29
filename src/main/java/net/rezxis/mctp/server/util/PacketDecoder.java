package net.rezxis.mctp.server.util;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class PacketDecoder extends ByteToMessageDecoder {

    private int length;

    public PacketDecoder() {
        length = -1;
    }

    @Override
    protected void decode(ChannelHandlerContext ch, ByteBuf buf, List<Object> out) throws Exception {
        if(length == -1){
            if(buf.readableBytes() < 4){
                return;
            }
            length = buf.readInt();
        }

        if(buf.readableBytes() < length){
            return;
        }


        ByteBuf o = buf.readBytes(length);
        out.add(o);
        length = -1;

    }

}