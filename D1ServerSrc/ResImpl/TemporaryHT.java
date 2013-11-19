package ResImpl;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/*
 * Just like an RMHastable, except that it stores a list of values in the order they go in,
 * so that the changes can be updated in the main RMHashtable later.
 */
public class TemporaryHT extends RMHashtable{
	LinkedBlockingQueue<Object[]> changeQueue;
	public TemporaryHT() {
		super();
		changeQueue = new LinkedBlockingQueue<Object[]>();
	}
	
	public synchronized Object put(Object key, Object value) {
		Object[] changes = {key, value};
		changeQueue.add(changes);
		return super.put(key, value);
	}
	
	public synchronized Object remove(Object key) {
		if (super.contains(key)) {
			return super.remove(key);
		}
		else {
			Object[] values = {key, null};
			try {
				changeQueue.put(values);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public LinkedBlockingQueue<Object[]> getChangeQueue() {
		return changeQueue;
	}

}
