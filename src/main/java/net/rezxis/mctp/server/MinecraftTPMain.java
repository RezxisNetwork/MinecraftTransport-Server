package net.rezxis.mctp.server;

import java.net.ServerSocket;
import java.net.Socket;

import net.rezxis.mctp.server.control.CInputStreamListener;

public class MinecraftTPMain {

	public static String host = "mctp1.rezxis.net";
	private static ServerSocket server;
	
	public static void main(String[] args) {
		if (args.length != 0)
			host = args[0];
		System.out.println("Host : "+host);
		try {
			server = new ServerSocket(9999);
			while (true) {
				Socket socket = server.accept();
				new Thread(new CInputStreamListener(socket)).start();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
