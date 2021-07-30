package net.rezxis.mctp.server;

import net.rezxis.mctp.server.prometheus.MCTPPrometheus;

import java.io.File;

public class MinecraftTPMain {
	
	public static void main(String[] args) {
		Console.info("Loading configuration file.");
		try {
			MCTPConfig.load(new File("mctp.json"));
		} catch (Exception e) {
			Console.error("Failed to load configuration file.");
			Console.exception(e);
			System.exit(-1);
			return;
		}
		new Thread(new MCTPPrometheus()).start();
		Console.info("Loaded configuration file.");
		Console.info("Starting MCTP-Server!");
		Console.info("Listen : "+MCTPConfig.instance.listen_host+":"+MCTPConfig.instance.listen_port);
		Console.info("Host : "+MCTPConfig.instance.host);
		Console.info("Allocated port range: "+MCTPConfig.instance.port_start+"->"+(MCTPConfig.instance.port_start+MCTPConfig.instance.port_range));
		new Thread(new MCTPServer()).start();
	}
}
