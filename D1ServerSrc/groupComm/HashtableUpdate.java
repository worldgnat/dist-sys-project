package groupComm;

import ResImpl.RMItem;
import ResInterface.ClientMidClock;

public class HashtableUpdate extends RMMessage {
	String key;
	RMItem value;
	ClientMidClock clock;
	
	public HashtableUpdate(int tid, String key, RMItem value, String sourceChannel, ClientMidClock clock) {
		super(tid, sourceChannel);
		this.key = key;
		this.clock = clock;
		this.value = value;
	}
	public String getKey() { return key; }
	public RMItem getValue() { return value; }
	public ClientMidClock getClock() { return clock; }
}
