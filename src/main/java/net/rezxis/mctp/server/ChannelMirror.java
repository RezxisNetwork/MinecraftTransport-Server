package net.rezxis.mctp.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.rezxis.mctp.server.prometheus.MCTPPrometheus;

@ChannelHandler.Sharable
public class ChannelMirror extends ChannelInboundHandlerAdapter {

    private final Channel dest;
    private final boolean notice;

    public ChannelMirror(Channel dest, boolean notice) {
        this.dest = dest;
        this.notice = notice;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (notice) {
            MCTPPrometheus.instance.decreaseConnectedSessions();
        }
        if (!dest.isActive())
            return;
        dest.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!dest.isActive()) {
            ctx.channel().close();
            return;
        }
        dest.writeAndFlush(msg);
    }
}