package groupComm;

import java.io.Serializable;

public class RequestPrimary implements Serializable {
	String responseChannel;
	
	public RequestPrimary(String responseChannel) {
		this.responseChannel = responseChannel;
	}
	
	public String getResponseChannel() { return responseChannel; }
}
