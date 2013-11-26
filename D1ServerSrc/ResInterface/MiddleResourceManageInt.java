package ResInterface;

import java.rmi.RemoteException;

import ResImpl.RMItem;
import exceptions.InvalidTransactionException;
import exceptions.TransactionAbortedException;

/*
 * Perhaps this is sloppy, but it captures all the methods that
 * ResourceManagerImpl and Middleware have in common. Those methods
 * happen to be all that is needed for GroupManagement to propogate changes
 * to an entire cluster of Middlewares or ResourceManagerImpls
 */
public interface MiddleResourceManageInt {
	public void start(int tid) throws RemoteException;
	public void commit(int tid) throws RemoteException,InvalidTransactionException,TransactionAbortedException;
	public void abort(int tid)  throws RemoteException, InvalidTransactionException;;
	
	public void writeData(int tid, String key, RMItem value);
	public RMItem removeData(int tid, String key);
}
