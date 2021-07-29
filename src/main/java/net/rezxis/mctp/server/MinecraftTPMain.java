package net.rezxis.mctp.server;

import java.net.ServerSocket;

public class MinecraftTPMain {

	public static String host = "mctp1.rezxis.net";
	public static String listen_host = "0.0.0.0";
	public static int listen_port = 9998;
	private static ServerSocket server;
	public static final int PORT_START = 40000;
	public static final int PORT_RANGE = 10000;
	
	public static void main(String[] args) {
		if (args.length != 3) {
			Console.error("usage : server.jar <host> <listen_address> <listen_port>");
			return;
		}
		host = args[0];
		listen_host = args[1];
		listen_port = Integer.valueOf(args[2]);

		Console.info("Starting MCTP-Server!");
		Console.info("Listen : "+listen_host+":"+listen_port);
		Console.info("Host : "+host);
		Console.info("Allocated port range: "+PORT_START+"->"+(PORT_START+PORT_RANGE));
		new Thread(new MCTPServer()).start();
	}
}
