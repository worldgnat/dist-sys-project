package groupComm;

import org.jgroups.JChannel;

public class RequestPrimary {
	String responseChannel;
	
	public RequestPrimary(String responseChannel) {
		this.responseChannel = responseChannel;
	}
	
	public String getResponseChannel() { return responseChannel; }
}
