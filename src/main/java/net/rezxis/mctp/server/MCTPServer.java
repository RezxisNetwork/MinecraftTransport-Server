package net.rezxis.mctp.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.rezxis.mctp.server.util.PacketDecoder;
import net.rezxis.mctp.server.util.PacketEncoder;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@ChannelHandler.Sharable
public class MCTPServer extends ChannelInboundHandlerAdapter implements Runnable {

    @Override
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
                            ch.pipeline().addLast(new PacketEncoder());
                            ch.pipeline().addLast(new PacketDecoder(), MCTPServer.this);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true);

            ChannelFuture future = bs.bind(MinecraftTPMain.listen_host,MinecraftTPMain.listen_port).sync();

            future.channel().closeFuture().sync();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        int op = buf.readByte();
        if (op == MCTPVars.CODE_INIT) {
            MCTPConnection connection = ServerManager.initConnection(ctx);
            ctx.attr(MCTPVars.CONNECTION_KEY).set(connection);
            ctx.writeAndFlush(createPacketReady(connection));
            Console.info("initialized mctp connection from " + connection.getIp());
        } else if (op == MCTPVars.CODE_UPGRADE) {
            long secret = buf.readLong();
            long id = buf.readLong();
            MCTPConnection connection = ServerManager.secretConnections.get(secret);
            connection.upgradeConnection(ctx, id);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (ctx.attr(MCTPVars.CONNECTION_KEY) == null)
            return;
        MCTPConnection connection = ctx.attr(MCTPVars.CONNECTION_KEY).get();
        connection.close();
        ServerManager.removeConnection(connection);
        Console.info("disconnected mctp connection from " + connection.getIp());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Console.info("occurred exception in mctp connnection from "+((InetSocketAddress)ctx.channel().remoteAddress()).getHostName());
        cause.printStackTrace();
        if (ctx.channel().isOpen()) {
            ctx.close();
        }
    }

    private ByteBuf createPacket(int size){
        return Unpooled.buffer(size, size);
    }

    private ByteBuf createPacketReady(MCTPConnection connection) {
        byte[] data = MinecraftTPMain.host.getBytes(StandardCharsets.UTF_8);
        ByteBuf packet = createPacket(9 + data.length);
        packet.writeByte(MCTPVars.CODE_READY);
        packet.writeInt(data.length);
        packet.writeBytes(data);
        packet.writeInt(connection.listener_port);
        return packet;
    }
}