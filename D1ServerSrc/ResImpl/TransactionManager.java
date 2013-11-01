package ResImpl;
import java.rmi.RemoteException;
import java.util.Enumeration;

import LockManager.*;

public class TransactionManager {
	
	LockManager lm;
	Middleware mid = null;
	
	public TransactionManager(Middleware creator){
		lm = new LockManager();
		mid = creator;
	}
	
	
	/*********************************
	*****        FLIGHTS            ****
	*********************************/
	public boolean reserveFlight(int id, int customerID, int flightNum) throws RemoteException
	{
		try {
			lm.Lock (id, Flight.getKey(flightNum), LockManager.WRITE);
			lm.Lock (id, Customer.getKey(customerID), LockManager.WRITE);
			return mid.reserveFlight(id,customerID,flightNum);
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return false;
		    }
	}
	
	public boolean deleteFlight(int id, int flightNum) throws RemoteException
	{
		try {
			lm.Lock (id, Flight.getKey(flightNum), LockManager.WRITE);
			return mid.deleteFlight(id,flightNum);
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return false;
		    }
	}
	public int queryFlight(int id, int flightNum) throws RemoteException
	{
		try {
			lm.Lock (id, Flight.getKey(flightNum), LockManager.READ);
			return mid.queryFlight(id,flightNum);
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return -1;
		    }
		
	}
	
	public int queryFlightPrice(int id, int flightNum) throws RemoteException
	{
		try {
			lm.Lock (id, Flight.getKey(flightNum), LockManager.READ);
			return mid.queryFlightPrice(id,flightNum);
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return -1;
		    }
		
	}
	
	/*********************************
	*****        CARS            ****
	*********************************/
	
	public boolean reserveCar(int id, int customerID, String location) throws RemoteException
	{
		try {
			lm.Lock (id, Car.getKey(location), LockManager.WRITE);
			lm.Lock (id, Customer.getKey(customerID), LockManager.WRITE);
			return mid.reserveCar(id,customerID,location);
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return false;
		    }
	}
	
	public boolean deleteCars(int id, String location) throws RemoteException
	{
		try {
			lm.Lock (id,  Car.getKey(location), LockManager.WRITE);
			return mid.deleteCars(id,location);
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return false;
		    }
	}
	
	public int queryCars(int id, String location) throws RemoteException
	{
		try {
			lm.Lock (id,  Car.getKey(location), LockManager.READ);
			return mid.queryCars(id,location);
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return -1;
		    }
		
	}
	
	public int queryCarsPrice(int id, String location) throws RemoteException
	{
		try {
			lm.Lock (id,  Car.getKey(location), LockManager.READ);
			return mid.queryCarsPrice(id,location);
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return -1;
		    }
		
	}
	
	
	/*********************************
	*****        ROOMS            ****
	*********************************/
	
	
	public boolean reserveRoom(int id, int customerID, String location) throws RemoteException
	{
		try {
			lm.Lock (id, Hotel.getKey(location), LockManager.WRITE);
			lm.Lock (id, Customer.getKey(customerID), LockManager.WRITE);
			return mid.reserveRoom(id,customerID,location);
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return false;
		    }
	}
	public boolean deleteRooms(int id, String location) throws RemoteException
	{
		try {
			lm.Lock (id,Hotel.getKey(location), LockManager.WRITE);
			return mid.deleteRooms(id,location);
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return false;
		    }
	}
	
	public int queryRooms(int id, String location) throws RemoteException
	{
		try {
			lm.Lock (id,Hotel.getKey(location), LockManager.READ);
			return mid.queryRooms(id,location);
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return -1;
		    }
		
	}
	
	public int queryRoomsPrice(int id, String location) throws RemoteException
	{
		try {
			lm.Lock (id,Hotel.getKey(location), LockManager.READ);
			return mid.queryRoomsPrice(id,location);
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return -1;
		    }
		
	}
	
	
	/**************************************
	 *         CUSTOMERS
	 **************************************/
	
	public boolean deleteCustomer(int id, int customerID) throws RemoteException
	{
		try{
			lm.Lock(id, Customer.getKey(customerID), LockManager.WRITE);
			Enumeration<ReservedItem> custRes = mid.getCustomerReservations(id, customerID).elements();
			
			for(; custRes.hasMoreElements();)
			{
				ReservedItem current = custRes.nextElement();
				lm.Lock(id, current.getKey(), LockManager.WRITE);
			}
			
			return mid.deleteCustomer(id, customerID);
		}
		 catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return false;
		    }
	}
	
	public String queryCustomerInfo(int id, int customerID) throws RemoteException
	{
		try{
			lm.Lock(id, Customer.getKey(customerID), LockManager.READ);
			return mid.queryCustomerInfo(id, customerID);
		}
		
		catch (DeadlockException e) { 
	        //System.out.println ("Deadlock.... ");
	        return "Deadlock";
	    }
	}
}
