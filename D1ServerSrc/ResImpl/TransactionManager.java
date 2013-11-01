package ResImpl;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Vector;

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
	public boolean reserveFlight(int id, int customerID, int flightNum)
	{
		try {
			boolean cusBool = false;
			boolean flightBool = false;
			flightBool = lm.Lock (id, Flight.getKey(flightNum), LockManager.WRITE);
			cusBool = lm.Lock (id, Customer.getKey(customerID), LockManager.WRITE);
			if (cusBool == true && flightBool == true)
			{ return true; } //mid.reserveFlight(id,customerID,flightNum); }
			else
			{ return false; }
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return false;
		    }
	}
	
	public boolean deleteFlight(int id, int flightNum) 
	{
		try {
			boolean flightBool = false;
			flightBool = lm.Lock (id, Flight.getKey(flightNum), LockManager.WRITE);
			if (flightBool)
			{ return true; } //mid.deleteFlight(id,flightNum); }
			else
			{ return false; }
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return false;
		    }
	}
	public boolean queryFlight(int id, int flightNum) 
	{
		try {
			boolean flightBool = false;
			flightBool = lm.Lock (id, Flight.getKey(flightNum), LockManager.READ);
			if (flightBool)
			{ return true; } //mid.queryFlight(id,flightNum); }
			else
			{ return false; }
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return false;
		    }
		
	}
	
	public boolean queryFlightPrice(int id, int flightNum) 
	{
		try {
			boolean flightBool = false;
			flightBool = lm.Lock (id, Flight.getKey(flightNum), LockManager.READ);
			if (flightBool)
			{ return true; } //mid.queryFlightPrice(id,flightNum); }
			else
			{ return false; }
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return false;
		    }
		
	}
	
	/*********************************
	*****        CARS            ****
	*********************************/
	
	public boolean reserveCar(int id, int customerID, String location)
	{
		try {
			boolean carBool = false;
			boolean cusBool = false;
			carBool = lm.Lock (id, Car.getKey(location), LockManager.WRITE);
			cusBool = lm.Lock (id, Customer.getKey(customerID), LockManager.WRITE);
			if (cusBool == true && carBool == true)
			{ return true; } //mid.reserveCar(id,customerID,location); }
			else
			{ return false; }
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return false;
		    }
	}
	
	public boolean deleteCars(int id, String location) 
	{
		try {
			boolean carBool = false;
			carBool = lm.Lock (id,  Car.getKey(location), LockManager.WRITE);
			if (carBool)
			{ return true; } //mid.deleteCars(id,location); }
			else
			{ return false; }
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return false;
		    }
	}
	
	public boolean queryCars(int id, String location) 
	{
		try {
			boolean carBool = false;
			carBool = lm.Lock (id,  Car.getKey(location), LockManager.READ);
			if (carBool)
			{ return true; }//mid.queryCars(id,location); }
			else
			{ return false; }
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return false;
		    }
		
	}
	
	public boolean queryCarsPrice(int id, String location)
	{
		try {
			boolean carBool = false;
			carBool = lm.Lock (id,  Car.getKey(location), LockManager.READ);
			if (carBool)
				{ return true;} //mid.queryCarsPrice(id,location); }
			else
			{ return false; }
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return false;
		    }
		
	}
	
	
	/*********************************
	*****        ROOMS            ****
	*********************************/
	
	
	public boolean reserveRoom(int id, int customerID, String location)
	{
		try {
			boolean cusBool = false;
			boolean roomBool = false;
			roomBool = lm.Lock (id, Hotel.getKey(location), LockManager.WRITE);
			cusBool = lm.Lock (id, Customer.getKey(customerID), LockManager.WRITE);
			if (cusBool == true && roomBool == true)
				{ return true; }//mid.reserveRoom(id,customerID,location); }
			else
			{ return false; }
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return false;
		    }
	}
	public boolean deleteRooms(int id, String location)
	{
		try {
			boolean roomBool = false;
			roomBool = lm.Lock (id,Hotel.getKey(location), LockManager.WRITE);
			if (roomBool)
				{ return true;}//mid.deleteRooms(id,location); }
			else
				{ return false; }
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return false;
		    }
	}
	
	public boolean queryRooms(int id, String location)
	{
		try {
			boolean roomBool = false;
			roomBool = lm.Lock (id,Hotel.getKey(location), LockManager.READ);
				if (roomBool)
					{ return true; }//mid.queryRooms(id,location); }
				else
					{ return false; }
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return false;
		    }
		
	}
	
	public boolean queryRoomsPrice(int id, String location) 
	{
		try {
			boolean roomBool = false;
			roomBool = lm.Lock (id,Hotel.getKey(location), LockManager.READ);
				if(roomBool)
				{ return true; } //mid.queryRoomsPrice(id,location); }
				else
				{ return false; }
		    }
		    catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return false;
		    }
		
	}
	
	
	/**************************************
	 *         CUSTOMERS
	 * @throws RemoteException 
	 **************************************/
	
	public boolean deleteCustomer(int id, int customerID) throws RemoteException
	{
		try{
			boolean cusBool = false;
			boolean reservedItemBool = false;
			
			cusBool = lm.Lock(id, Customer.getKey(customerID), LockManager.WRITE);
			@SuppressWarnings("unchecked")
			Enumeration<ReservedItem> custRes = mid.getCustomerReservations(id, customerID).elements();
			
			for(; custRes.hasMoreElements();)
			{
				ReservedItem current = custRes.nextElement();
				reservedItemBool = lm.Lock(id, current.getKey(), LockManager.WRITE);
			}
			
			
			if (cusBool && reservedItemBool)
			{ return true; }//mid.deleteCustomer(id, customerID); }
			
			else
			{ return false; }
		}
		 catch (DeadlockException e) { 
		        //System.out.println ("Deadlock.... ");
		        return false;
		    }
	}
	
	public boolean queryCustomerInfo(int id, int customerID) throws RemoteException
	{
		try{
			boolean cusBool = false;
			cusBool = lm.Lock(id, Customer.getKey(customerID), LockManager.READ);
			
			return cusBool;
		}
		
		catch (DeadlockException e) { 
	        //System.out.println ("Deadlock.... ");
	        return false;
	    }
	}
	
	public boolean itinerary(int id,int customer,Vector flightNumbers,String location,boolean car,boolean room)
	{
		try{
			boolean cusBool = false;
			boolean roomBool = false;
			boolean carBool = false;
			boolean flightBool = false;
			
			cusBool = lm.Lock(id, Customer.getKey(customer), LockManager.WRITE);
			
			if (car==true)
	    	{
				carBool = lm.Lock (id, Car.getKey(location), LockManager.WRITE);
	    	}
	    	
	    	if (room==true)
	    	{
	    		roomBool = lm.Lock (id, Hotel.getKey(location), LockManager.WRITE);
	    	}
			
			for (int i=0; i <flightNumbers.size(); i++)
	    	{ 
	    		int flightNum = Integer.parseInt((String)flightNumbers.get(i));
	    		flightBool = lm.Lock (id, Flight.getKey(flightNum), LockManager.WRITE);
	    	}
			
	    	
	    	if (cusBool && carBool && roomBool && flightBool)
	    	{
	    		return true;
	    	}
	    	
	    	else
	    	{
	    		return false;
	    	}
		}
		
		catch (DeadlockException e) { 
	        //System.out.println ("Deadlock.... ");
	        return false;
	    }
	}
}
