package groupComm;

import ResImpl.RMItem;

public class HashtableUpdate extends RMMessage {
	String key;
	RMItem value;
	
	public HashtableUpdate(int tid, String key, RMItem value, String sourceChannel) {
		super(tid, sourceChannel);
		this.key = key;
		this.value = value;
	}
	public String getKey() { return key; }
	public RMItem getValue() { return value; }
}
