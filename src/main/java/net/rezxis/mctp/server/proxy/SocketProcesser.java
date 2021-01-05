package net.rezxis.mctp.server.proxy;

import java.net.InetSocketAddress;
import java.net.Socket;

import net.rezxis.mctp.server.control.CInputStreamListener;

public class SocketProcesser implements Runnable {

	protected Socket socket;
	protected Socket endpoint;
	private ProxyProtocol protocol;
	private CInputStreamListener cis;
	
	public SocketProcesser(Socket socket, Socket endpoint, ProxyProtocol protocol, CInputStreamListener cis) {
		this.socket = socket;
		this.endpoint = endpoint;
		this.protocol = protocol;
		this.cis = cis;
	}
	
	@Override
	public void run() {
		String source = endpoint.getInetAddress().getHostAddress();
		int sourcePort = ((InetSocketAddress)endpoint.getRemoteSocketAddress()).getPort();
		String dest = endpoint.getLocalAddress().getHostAddress();
		int destPort = ((InetSocketAddress)endpoint.getLocalSocketAddress()).getPort();
		String header = "PROXY TCP4 "+source+" "+dest+" "+sourcePort+" "+destPort+"\r\n";
		System.out.println(String.format("accepted : %s:%d to %s:%d", source, sourcePort, dest, destPort));
		try {
			endpoint.setTcpNoDelay(true);
			socket.setTcpNoDelay(true);
			
			socket.getOutputStream().write(header.getBytes(), 0, header.getBytes().length);
			
			new Thread(new SocketTransporter(endpoint.getInputStream(), socket.getOutputStream(), new CloseCallback(this,cis))).start();
			new Thread(new SocketTransporter(socket.getInputStream(), endpoint.getOutputStream(), new CloseCallback(this,cis))).start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void close() {
		if (socket.isConnected())
			try {socket.close();} catch (Exception ex) {ex.printStackTrace();}
		if (endpoint.isConnected())
			try {endpoint.close();} catch (Exception ex) {ex.printStackTrace();}
	}
}
