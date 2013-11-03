package ResImpl;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import LockManager.*;

public class TransactionManager {
	
	LockManager lm;
	Middleware mid = null;
	List<Integer> tList = null;    // List that keeps track of all transactions
	
	public TransactionManager(/*Middleware creator*/){
		lm = new LockManager();
		tList = new ArrayList<Integer>();
		//mid = creator;
	}
	
	
	/****************************
	 * Transaction functions **
	 ******************************/

	public void start(int tid)
	{
		if (!tList.contains(tid))
		{
			tList.add(tid);   // we enlist the transaction
		}
		
	}
	
	// Release all the locks and remove the transaction from the list
	public void commit(int tid)
	{
		lm.UnlockAll(tid);                          
		int index = tList.indexOf((Integer)tid);
		tList.remove(index);
	}
	
	/*********************************
	*****        FLIGHTS            ****
	*********************************/
	public boolean reserveFlight(int id, int customerID, int flightNum)
	{
		try {
			
			// Obtain locks for the customer and flight objects. If you have both
			// then you return true. YOU NEED WRITE LOCKS SINCE YOU CHANGE
			// THE NUMBER OF SEATS AND THE RESERVATIONS OF THE CUSTOMER.
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
			// Obtain a WRITE lock since you are deleting the flight
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
			// We only need a READ lock for the flight
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
			// We only need a READ lock since we wish to know the price
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
			// Obtain a WRITE lock on the car and customer objects because you need
			// to change the reservations and number of cars values
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
			// Only need a WRITE lock on the car object. If the customer still
			// exists, then the middleware will reject the query anyways. Therefore
			// we don't need to worry about the customers who reserved the cars.
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
			// Only need a READ lock
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
			// Only need a READ lock
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
			// We need an exclusive lock on both customer and room
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
			// Need an exclusive lock on the room
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
			// Need a shared lock on the room
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
			// Only need a shared lock on the room
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
	/*
	public boolean deleteCustomer(int id, int customerID) throws RemoteException
	{
		try{
			// In here, we need exclusive locks on the customer as well
			// any flights, cars, rooms that he/she has reserved.
			boolean cusBool = false;
			boolean reservedItemBool = false;
			
			cusBool = lm.Lock(id, Customer.getKey(customerID), LockManager.WRITE);
			
			
			@SuppressWarnings("unchecked")
			Enumeration<ReservedItem> custRes = mid.getCustomerReservations(id, customerID).elements();
			
			// Acquire exclusive locks on all reserved items
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
	}*/
	
	public boolean queryCustomerInfo(int id, int customerID)
	{
		try{
			// Only need a shared lock on the customer
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
			
			// We need exclusive locks on the customer, hotel, car AND every flight in the vector
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
/*
 class Hotel_Car_Tuple{
	String location;
	int numItems;
	int price;
	
	public Hotel_Car_Tuple(String location, int numItems, int price){
		this.numItems = numItems;
		this.location = location;
		this.price = price;
	}
	
	public String getLocation(){
		return this.location;
	}
	public int getNumItems(){
		return this.numItems;
	}
	public int getPrice(){
		return this.price;
	}
}
 
 class Flight_Tuple{
	 int flightNum;
	 int numSeats;
	 int price;
	 
	 public Flight_Tuple(int flightNum, int numSeats, int price){
		 this.flightNum = flightNum;
		 this.numSeats = numSeats;
		 this.price = price;
	 }
	 
	 public int getFlightNum(){
		 return this.flightNum;
	 }
	 public int getSeats(){
		 return this.numSeats;
	 }
	 public int getPrice(){
		 return this.price;
	 }
 }
 
 class Customer_Tuple{
	 int custId;
	 public Customer_Tuple(){
		 
	 }
 }*/
