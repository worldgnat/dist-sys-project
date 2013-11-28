package ResInterface;

import java.rmi.RemoteException;

import ResImpl.RMHashtable;
import ResImpl.RMItem;
import exceptions.InvalidTransactionException;
import exceptions.TransactionAbortedException;
import groupComm.ImThePrimary;

/*
 * Perhaps this is sloppy, but it captures all the methods that
 * ResourceManagerImpl and Middleware have in common. Those methods
 * happen to be all that is needed for GroupManagement to propogate changes
 * to an entire cluster of Middlewares or ResourceManagerImpls
 */
public interface MiddleResourceManageInt {
	RMHashtable m_itemHT = null;
	public void start(int tid, ClientMidClock clock) throws RemoteException;
	public void commit(int tid, ClientMidClock clock) throws RemoteException,InvalidTransactionException,TransactionAbortedException;
	public void abort(int tid, ClientMidClock clock)  throws RemoteException, InvalidTransactionException;;
	
	public void writeData(int tid, String key, RMItem value, ClientMidClock clock);
	public RMItem removeData(int tid, String key);
	
	public void setPrimary(String hostname, int port, String type);
	
	public int getPort();
	public String getBinding();
}
