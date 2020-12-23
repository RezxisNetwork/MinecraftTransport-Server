package net.rezxis.mctp.server.control;

import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import net.rezxis.mctp.server.MinecraftTPMain;
import net.rezxis.mctp.server.proxy.ProxyProtocol;
import net.rezxis.mctp.server.proxy.ProxySocketListener;
import net.rezxis.mctp.server.proxy.SocketProcesser;

public class CInputStreamListener implements Runnable {

	public static HashMap<Integer, Socket> sockets = new HashMap<>();
	private Socket socket;
	private static int portPos = 40000;
	private int port;
	public ArrayList<SocketProcesser> sps = new ArrayList<>();
	private ProxySocketListener psl = null;
	
	public CInputStreamListener(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		boolean close = true;
		try {
			InputStream is = socket.getInputStream();
			byte[] buffer = new byte[4096];
			int len;
			while (-1 != (len = is.read(buffer))) {
				if (buffer[0] == 0x1) {
					portPos += 1;
					port = portPos;
					String ip = MinecraftTPMain.host+":"+port;
					psl = new ProxySocketListener(port,socket);
					new Thread(psl).start();
					ByteBuffer bb = ByteBuffer.allocate(1+4+ip.getBytes().length);
					bb.put((byte) 0x1).putInt(ip.getBytes().length).put(ip.getBytes());
					socket.getOutputStream().write(bb.array(), 0, bb.capacity());
				} else if (buffer[0] == 0x2) {
					close = false;
					int id = ByteBuffer.wrap(buffer, 0, len).getInt(1);
					System.out.println("queue was accepted : "+id);
					Socket target = CInputStreamListener.sockets.get(id);
					SocketProcesser sp = new SocketProcesser(socket, target, ProxyProtocol.V2, this);
					sp.run();
					sps.add(sp);
					break;
				} else {
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		//closed
		if (close) {
			for (int i = 0; i < sps.size(); i++)
				sps.get(i).close();
			if (psl != null)
				psl.close();
		}
	}
}
