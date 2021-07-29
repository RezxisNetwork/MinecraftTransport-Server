package net.rezxis.mctp.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import net.rezxis.mctp.server.proxied.ProxiedServer;
import net.rezxis.mctp.server.util.PacketDecoder;
import net.rezxis.mctp.server.util.PacketEncoder;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MCTPConnection {

    public final ChannelHandlerContext ctx;
    public int listener_port;
    public ProxiedServer proxy;
    public HashMap<Long,ChannelHandlerContext> children;
    public long current_id = 0;
    public long secret;

    protected MCTPConnection(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public void init() {
        this.listener_port = MinecraftTPMain.PORT_START + new Random().nextInt(MinecraftTPMain.PORT_RANGE);
        secret = new Random().nextLong();
        children = new HashMap<>();
        proxy = new ProxiedServer(this);
        new Thread(proxy).start();
    }

    public void activeProxied(ChannelHandlerContext ctx) {
        long id = ++current_id;
        children.put(id, ctx);
        sendNewPacket(id);
    }

    private ByteBuf createPacket(int size){
        return Unpooled.buffer(size, size);
    }

    private void sendNewPacket(long id) {
        ByteBuf packet = createPacket(17);
        packet.writeByte(MCTPVars.CODE_NEW);
        packet.writeLong(secret);
        packet.writeLong(id);
        ctx.writeAndFlush(packet);
    }

    public void upgradeConnection(ChannelHandlerContext ctx1, long id) {
        ctx1.pipeline().remove(PacketDecoder.class);
        ctx1.pipeline().remove(PacketEncoder.class);
        ctx1.pipeline().remove(MCTPServer.class);
        ChannelHandlerContext waiting = children.get(id);
        waiting.pipeline().remove(ProxiedServer.class);
        for (ByteBuf buf : waiting.attr(MCTPVars.PACKET_STACK).get()) {
            ctx1.writeAndFlush(buf);
        }
        waiting.pipeline().addLast(new ChannelMirror(ctx1.channel()));
        ctx1.pipeline().addLast(new ChannelMirror(waiting.channel()));
    }

    public void close() {
        for (Map.Entry<Long,ChannelHandlerContext> entry : children.entrySet()) {
            entry.getValue().close();
        }
        proxy.channel.close();
    }

    public String getIp() {
        return ((InetSocketAddress)ctx.channel().remoteAddress()).getHostName();
    }
}
