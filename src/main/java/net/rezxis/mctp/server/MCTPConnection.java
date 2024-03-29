package net.rezxis.mctp.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import net.rezxis.mctp.server.prometheus.MCTPPrometheus;
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
        this.listener_port = MCTPConfig.instance.port_start + new Random().nextInt(MCTPConfig.instance.port_range);
        secret = new Random().nextLong();
        children = new HashMap<>();
        proxy = new ProxiedServer(this);
        MCTPPrometheus.instance.increaseConnectedMCTPServers();
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
        InetSocketAddress local_addr = (InetSocketAddress) waiting.channel().localAddress();
        InetSocketAddress remote_addr = (InetSocketAddress) waiting.channel().remoteAddress();
        Console.info("PROXY TCP4 "+remote_addr.getHostString()+" "+local_addr.getHostString()+" "+remote_addr.getPort()+" "+local_addr.getPort());
        byte[] header = ("PROXY TCP4 "+remote_addr.getHostString()+" "+local_addr.getHostString()+" "+remote_addr.getPort()+" "+local_addr.getPort()+"\r\n").getBytes();
        ByteBuf data = Unpooled.buffer(header.length,header.length);
        data.writeBytes(header);
        ctx1.writeAndFlush(data);
        waiting.pipeline().remove(ProxiedServer.class);
        for (ByteBuf buf : waiting.attr(MCTPVars.PACKET_STACK).get()) {
            ctx1.writeAndFlush(buf);
        }
        waiting.pipeline().addLast(new ChannelMirror(ctx1.channel(), false));
        ctx1.pipeline().addLast(new ChannelMirror(waiting.channel(), true));
        MCTPPrometheus.instance.increaseConnectedSessions();
    }

    public void close() {
        for (Map.Entry<Long,ChannelHandlerContext> entry : children.entrySet()) {
            entry.getValue().close();
        }
        proxy.channel.close();
    }

    public String getIp() {
        return ((InetSocketAddress)ctx.channel().remoteAddress()).getHostString();
    }
}
