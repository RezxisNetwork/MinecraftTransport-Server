package net.rezxis.mctp.server.control;

import java.net.ServerSocket;
import java.net.Socket;

public class CSocketListener implements Runnable {

	private ServerSocket server;
	
	public void run() {
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
