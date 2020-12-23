package net.rezxis.mctp.server;

import net.rezxis.mctp.server.control.CSocketListener;

public class MinecraftTPMain {

	public static String host = "mctp1.rezxis.net";
	
	public static void main(String[] args) {
		if (args.length != 0)
			host = args[0];
		System.out.println("Host : "+host);
		new Thread(new CSocketListener()).start();
		while(true) {}
	}
}
