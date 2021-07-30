package net.rezxis.mctp.server.proxied;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.rezxis.mctp.server.*;
import net.rezxis.mctp.server.prometheus.MCTPPrometheus;

import java.util.ArrayList;

@ChannelHandler.Sharable
public class ProxiedServer extends ChannelInboundHandlerAdapter implements Runnable {

    private MCTPConnection connection;
    public Channel channel;

    public ProxiedServer(MCTPConnection connection) {
        this.connection = connection;
    }

    public void run() {
        EventLoopGroup worker, boss;
        worker = new NioEventLoopGroup();
        boss = new NioEventLoopGroup();

        ServerBootstrap bs = new ServerBootstrap();
        try {
            bs.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(ProxiedServer.this);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true);

            ChannelFuture future = bs.bind(MCTPConfig.instance.listen_host,connection.listener_port).sync();
            channel = future.channel();
            future.channel().closeFuture().sync();
        } catch (Exception ex) {
            Console.exception(ex);
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.attr(MCTPVars.PACKET_STACK).set(new ArrayList<>());
        connection.activeProxied(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.attr(MCTPVars.PACKET_STACK).get().add((ByteBuf)msg);
    }
}
