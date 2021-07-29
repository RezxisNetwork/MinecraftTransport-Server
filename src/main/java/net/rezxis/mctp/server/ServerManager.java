package net.rezxis.mctp.server;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.HashMap;

public class ServerManager {

    private static ArrayList<MCTPConnection> connections = new ArrayList<>();
    public static HashMap<Long,MCTPConnection> secretConnections = new HashMap<>();

    public static MCTPConnection initConnection(ChannelHandlerContext ctx) {
        MCTPConnection connection = new MCTPConnection(ctx);
        connection.init();
        connections.add(connection);
        secretConnections.put(connection.secret, connection);
        return connection;
    }

    public static void removeConnection(MCTPConnection connection) {
        connections.remove(connection);
    }
}
