package groupComm;

import java.io.Serializable;

public class RequestPrimary implements Serializable {
	String responseChannel;
	String hostname;
	int port;
	
	public RequestPrimary(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
	}
	
	public String getHostname() { return hostname; }
	public int getPort() { return port; }
}
