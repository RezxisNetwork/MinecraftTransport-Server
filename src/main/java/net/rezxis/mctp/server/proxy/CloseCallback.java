package net.rezxis.mctp.server.proxy;

import net.rezxis.mctp.server.control.CInputStreamListener;

public class CloseCallback implements Runnable {

	private SocketProcesser sp;
	private CInputStreamListener cis;
	
	public CloseCallback(SocketProcesser sp, CInputStreamListener cis) {
		this.sp = sp;
		this.cis = cis;
	}
	
	@Override
	public void run() {
		if (sp.endpoint.isConnected())
			try { sp.endpoint.close(); } catch (Exception ex) {ex.printStackTrace();}
		if (sp.socket.isConnected())
			try { sp.socket.close(); } catch (Exception ex) {ex.printStackTrace();}
		if (cis.sps.contains(sp)) {
			cis.sps.remove(sp);
		}
	}
}
