package ResInterface;

import java.io.Serializable;

public class ClientMidClock implements Serializable,Cloneable {
	String hostname;
	long clock;
	
	public ClientMidClock(String hostname) {
		this.hostname = hostname;
		this.clock = 0;
	}
	
	public String getHostname() { return hostname; }
	public long getClock() { return clock; }
	public ClientMidClock increment() { 
		clock++; 
		try { 
			return (ClientMidClock)this.clone();
		}
		catch (Exception er) {
			er.printStackTrace();
		}
		return null;
	}
}
