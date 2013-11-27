package groupComm;

public class RMMessage {
	int tid;
	String sourceChannel;
	
	public RMMessage(int tid, String sourceChannel) {
		this.tid = tid;
		this.sourceChannel = sourceChannel;
	}
	
	public int getTid() { return tid; }
	public String getSourceChannel() { return sourceChannel; }
}
