package net.rezxis.mctp.server.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import net.rezxis.mctp.server.control.CInputStreamListener;

public class ProxySocketListener implements Runnable {

	private ServerSocket server;
	private int port;
	private Socket cSocket;
	private int current;
	
	public ProxySocketListener(int port, Socket cSocket) {
		this.port = port;
		this.cSocket = cSocket;
	}
	
	public void run() {
		try {
			System.out.println("Listening on "+port+" (GAMEUSER -> {MCTP_SERVER} -> MCTP_CLIENT -> SPIGOT)");
			server = new ServerSocket(port);
			while (true) {
				Socket socket = server.accept();
				current += 1;
				CInputStreamListener.sockets.put(current, socket);
				byte[] array = ByteBuffer.allocate(5).put((byte)0x2).putInt(current).array();
				cSocket.getOutputStream().write(array, 0, array.length);
				System.out.println("A connection was connected : "+current+ " : "+port);
			}
		} catch (Exception ex) {
			if (!ex.getMessage().contains("Socket closed"))
				ex.printStackTrace();
		}
	}
	
	public void close() {
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
