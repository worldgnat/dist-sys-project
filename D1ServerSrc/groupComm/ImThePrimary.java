package groupComm;

public class ImThePrimary {
	String hostname;
	int port;
	String channel;
	
	public ImThePrimary(String hostname, int port, String channel) {
		this.hostname = hostname;
		this.port = port;
		this.channel = channel;
	}
	
	public String getHostname() { return hostname; }
	public int getPort() { return port; }
	
	public String getChannel() { return channel; }
}
