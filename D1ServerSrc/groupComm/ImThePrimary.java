package groupComm;

public class ImThePrimary {
	String hostname;
	int port;
	
	public ImThePrimary(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
	}
	
	public String getHostname() { return hostname; }
	public int getPort() { return port; }
}
