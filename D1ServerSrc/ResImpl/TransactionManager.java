 	

package ResImpl;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import exceptions.InvalidTransactionException;
import LockManager.*;

public class TransactionManager {
        //Timeout in milliseconds
        static final long timeout = 10000;
        LockManager lm;
        Middleware mid = null;
        TreeMap<Integer, Long> tList = null; // List that keeps track of all transactions
        //TransactionKicker kicker = null;
        public TransactionManager(Middleware creator){
                lm = new LockManager();
                tList = new TreeMap<Integer,Long>();
                mid = creator;
		//kicker = new TransactionKicker(this);
        }
        
        
        /****************************
         * Transaction functions **
         ******************************/

        public void start()
        {
                List<Integer> sorted = new LinkedList<Integer>();
                sorted.addAll(tList.keySet());
                int tid = sorted.get(sorted.size()-1) + 1;
                start(tid);
        }
        public void start(int tid) {
                synchronized(tList) {
                        if (!tList.containsKey(tid))
                        {
                                tList.put(tid, System.currentTimeMillis()); // we enlist the transaction and initialize the timer to now
                        }
                }
        }
        
        public void commit(int tid) throws InvalidTransactionException {
                synchronized(tList) {
                        if (!tList.containsKey(tid)) {
                                throw new InvalidTransactionException(tid);
                        }
                        else {
				System.out.println("TM: committing transaction " + tid);
                                tList.remove(tid);
                                lm.UnlockAll(tid);
                        }
                }
        }
        
        // Release all the locks and remove the transaction from the list
        public void abort(int tid) throws RemoteException,InvalidTransactionException
        {
                synchronized(tList) {
                	if (tList.containsKey(tid)) {
			System.out.println("TM: aborting transaction " + tid);	
                	lm.UnlockAll(tid);
                        tList.remove(tid);
                        mid.tmAbort(tid);
                	}
                	else {
                		throw new InvalidTransactionException(tid);
                	}
			
                }
        }
        
        /*********************************
        ***** FLIGHTS ****
        *********************************/
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) throws RemoteException, InvalidTransactionException {
		try{
			synchronized (tList) {
				if (tList.containsKey(id) == false){ throw new InvalidTransactionException(id); }
			}
					return (lm.Lock(id,Flight.getKey(flightNum),LockManager.WRITE));
					
		}


		catch (DeadlockException e)
		{ 
			System.out.println("Deadlock with transaction " + id + "when adding flight" + flightNum);
			abort(id);
			return false;
		}
			
	}
        public boolean reserveFlight(int id, int customerID, int flightNum)
        throws RemoteException, InvalidTransactionException 
        {
                try {
                        synchronized (tList) {
                                if (tList.containsKey(id) == false) {  throw new InvalidTransactionException(id); }
                        }
                                        // Obtain locks for the customer and flight objects. If you have both
                                        // then you return true. YOU NEED WRITE LOCKS SINCE YOU CHANGE
                                        // THE NUMBER OF SEATS AND THE RESERVATIONS OF THE CUSTOMER.
                                        boolean cusBool = false;
                                        boolean flightBool = false;
                                        flightBool = lm.Lock (id, Flight.getKey(flightNum), LockManager.WRITE);
                                        cusBool = lm.Lock (id, Customer.getKey(customerID), LockManager.WRITE);
                                        if (cusBool == true && flightBool == true)
                                        {
//                                                mid.reserveFlight(id,customerID,flightNum);
                                                return true;
                                        }
                                        else return false;
              }
                      
                
                catch (DeadlockException e) {
                    System.out.println ("Deadlock at transation " + id + "when reserving flight" + flightNum);
                    abort(id);
                    return false;
                }
        }
        
        public boolean deleteFlight(int id, int flightNum) throws InvalidTransactionException, RemoteException
        {
                try {
                        synchronized (tList) {
                                if (tList.containsKey(id) == false) {  throw new InvalidTransactionException(id); }
                        }
                                        // Obtain a WRITE lock since you are deleting the flight
                                        boolean flightBool = false;
                                        flightBool = lm.Lock (id, Flight.getKey(flightNum), LockManager.WRITE);
                                        if (flightBool)
                                        { return true; } //mid.deleteFlight(id,flightNum); }
                                        else
                                        { return false; }
                }
                        
               
         catch (DeadlockException e) {
        	 System.out.println ("Deadlock at transation " + id + "when reserving flight " + flightNum);
        	 abort(id);
        	 return false;
         	}

        }
        
        public boolean queryFlight(int id, int flightNum) throws InvalidTransactionException, RemoteException
        {
                try {
                        synchronized (tList) {
                                if (tList.containsKey(id) == false) {  throw new InvalidTransactionException(id); }
                        }
                                        // We only need a READ lock for the flight
                                        boolean flightBool = false;
                                        flightBool = lm.Lock (id, Flight.getKey(flightNum), LockManager.READ);
                                        if (flightBool)
                                        { return true; } //mid.queryFlight(id,flightNum); }
                                        else
                                        { return false; }
                       
                }
                
         catch (DeadlockException e) {
        	 System.out.println ("Deadlock at transation " + id + "when fetching flight " + flightNum);
        	 abort(id);
        	 return false;
         }

        }
        
        public boolean queryFlightPrice(int id, int flightNum) throws InvalidTransactionException, RemoteException 
        {
                try {
                        synchronized (tList) {
                                if (tList.containsKey(id) == false) {  throw new InvalidTransactionException(id); }
                        }
                                        // We only need a READ lock since we wish to know the price
                                        boolean flightBool = false;
                                        flightBool = lm.Lock (id, Flight.getKey(flightNum), LockManager.READ);
                                        if (flightBool)
                                        { return true; } //mid.queryFlightPrice(id,flightNum); }
                                        else
                                        { return false; }
                    
         }
         catch (DeadlockException e) {
        	 System.out.println ("Deadlock at transaction " + id + "when fetching flight price " + flightNum);
        	 abort(id);
        	 return false;
         }

        }
        
        /*********************************
        ***** CARS ****
        *********************************/

	public boolean addCars(int id, String location, int count, int price) throws InvalidTransactionException, RemoteException {
		try {
			synchronized(tList){
				if (tList.containsKey(id) == false){  throw new InvalidTransactionException(id); }
			}
					if (lm.Lock(id,Car.getKey(location),LockManager.WRITE))
					{ return true; }
					else
					{ return false; }
		
			
		}


		catch(DeadlockException e) {
			System.out.println("Deadlock at transaction " + id + "when adding car " + location);
			abort(id); 
			return false; 
		}
		

	}
        
        public boolean reserveCar(int id, int customerID, String location) throws InvalidTransactionException, RemoteException 
        {
                try {
                        synchronized (tList) {
                                if (tList.containsKey(id) == false) {  throw new InvalidTransactionException(id); }
                        }
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
        	 System.out.println ("Deadlock at transaction " + id + "when reserving car " + location);
        	 abort(id);
        	 return false;
         }
      
        }
        
        public boolean deleteCars(int id, String location) throws InvalidTransactionException, RemoteException 
        {
                try {
                        synchronized (tList) {
                                if (tList.containsKey(id) == false) {  throw new InvalidTransactionException(id); }
                        }
                                        // Only need a WRITE lock on the car object. If the customer still
                                        // exists, then the middleware will reject the query anyways. Therefore
                                        // we don't need to worry about the customers who reserved the cars.
                                        boolean carBool = false;
                                        carBool = lm.Lock (id, Car.getKey(location), LockManager.WRITE);
                                        if (carBool)
                                        { return true; } //mid.deleteCars(id,location); }
                                        else
                                        { return false; }
                        
         }
         catch (DeadlockException e) {
        	 System.out.println ("Deadlock at transction "+id+" when deleting car "+location);
        	 abort(id);
        	 return false;
         }

        }
        
        public boolean queryCars(int id, String location) throws InvalidTransactionException, RemoteException 
        {
                try {
                        synchronized (tList) {
                                if (tList.containsKey(id) == false) {  throw new InvalidTransactionException(id); }
                        }
                                        // Only need a READ lock
                                        boolean carBool = false;
                                        carBool = lm.Lock (id, Car.getKey(location), LockManager.READ);
                                        if (carBool)
                                        { return true; }//mid.queryCars(id,location); }
                                        else
                                        { return false; }
                      
         }
         catch (DeadlockException e) {
        	 System.out.println ("Deadlock at transaction " + id + " when detching car " + location);
        	 abort(id);
        	 return false;
         }
               
        }
        
        public boolean queryCarsPrice(int id, String location) throws InvalidTransactionException, RemoteException 
        {
                try {
                        synchronized (tList) {
                                if (tList.containsKey(id) == false) {  throw new InvalidTransactionException(id); }
                        }
                                        // Only need a READ lock
                                        boolean carBool = false;
                                        carBool = lm.Lock (id, Car.getKey(location), LockManager.READ);
                                        if (carBool)
                                                { return true;} //mid.queryCarsPrice(id,location); }
                                        else
                                        { return false; }
                                
         }
         catch (DeadlockException e) {
        	 System.out.println ("Deadlock at transaction " + id + " when fetching car price " + location);
        	 abort(id);
        	 return false;
         }
               
        }
        
        
        /*********************************
        ***** ROOMS ****
        *********************************/
        
        public boolean addRooms(int id, String location, int count, int price) throws InvalidTransactionException, RemoteException {
		try{
			synchronized (tList){
				if (tList.containsKey(id) == false ){  throw new InvalidTransactionException(id); }
			}
					if (lm.Lock(id,Hotel.getKey(location), LockManager.WRITE))
					{ return true; }
					else
					{ return false; }
			

		}


		catch (DeadlockException e) {
			System.out.println("Deadlock at transction " + id + " when adding rooms " + location); 
			abort(id);
			return false; 
		}
		
	}


        public boolean reserveRoom(int id, int customerID, String location) throws InvalidTransactionException, RemoteException 
        {
                try {
                        synchronized (tList) {
                                if (tList.containsKey(id) == false) {  throw new InvalidTransactionException(id); }
                        }
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
        	 System.out.println ("Deadlock at transaction " + id + " when reserving room " + location);
        	 abort(id); 
        	 return false;
         }
              
        }
        
        public boolean deleteRooms(int id, String location) throws InvalidTransactionException, RemoteException 
        {
                try {
                        synchronized (tList) {
                                if (tList.containsKey(id) == false) {  throw new InvalidTransactionException(id); }
                        }
                                        // Need an exclusive lock on the room
                                        boolean roomBool = false;
                                        roomBool = lm.Lock (id,Hotel.getKey(location), LockManager.WRITE);
                                        if (roomBool)
                                                { return true;}//mid.deleteRooms(id,location); }
                                        else
                                                { return false; }
                         
         }
         catch (DeadlockException e) {
        	 System.out.println ("Deadlock at transaction " + id + " when deleting rooms " + location);
        	 abort(id); 
        	 return false;
         }
                
        }
        
        public boolean queryRooms(int id, String location) throws InvalidTransactionException, RemoteException 
        {
                try {
                        synchronized (tList) {
                                if (tList.containsKey(id) == false) {  throw new InvalidTransactionException(id); }
                        }
                                        // Need a shared lock on the room
                                        boolean roomBool = false;
                                        roomBool = lm.Lock (id,Hotel.getKey(location), LockManager.READ);
                                        if (roomBool)
                                                { return true; }//mid.queryRooms(id,location); }
                                        else
                                                { return false; }
                             
         }
         catch (DeadlockException e) {
        	 System.out.println ("Deadlock at transaction " + id + " when fetching rooms " + location);
        	 abort(id); 
        	 return false;
         }
               
        }
        
        public boolean queryRoomsPrice(int id, String location) throws InvalidTransactionException, RemoteException 
        {
                try {
                        synchronized (tList) {
                                if (tList.containsKey(id) == false) {  throw new InvalidTransactionException(id); }
                        }
                                        // Only need a shared lock on the room
                                        boolean roomBool = false;
                                        roomBool = lm.Lock (id,Hotel.getKey(location), LockManager.READ);
                                        if(roomBool)
                                        { return true; } //mid.queryRoomsPrice(id,location); }
                                        else
                                        { return false; }
                         
         }
         catch (DeadlockException e) {
        	 System.out.println ("Deadlock at transaction " + id + "when fetching rooms price " + location);
        	 abort(id);
        	 return false;
         }
                
        }
        
        
        /**************************************
         * CUSTOMERS
         * @throws RemoteException
         **************************************/
	public boolean newCustomer(int id, int cid) throws InvalidTransactionException, RemoteException {
		try {
			synchronized (tList) {
				if (tList.containsKey(id) == false){  throw new InvalidTransactionException(id); }
			}
					if (lm.Lock(id,Customer.getKey(cid),LockManager.WRITE))
					{ return true; }
					else
					{ return false; }
		}


		catch (DeadlockException e)
		{ System.out.println("Deadlock at transaction " + id + "when adding customer " + cid); abort(id); return false; }
		
	}
        public boolean deleteCustomer(int id, int customerID) throws RemoteException, InvalidTransactionException 
        {
                try{
                        synchronized (tList) {
                                if (tList.containsKey(id) == false) {  throw new InvalidTransactionException(id); }
                        }
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
                	System.out.println ("Deadlock at transaction " + id + "when deleting customer " + customerID);
                	abort(id);
                	return false;
         }
             
        }
        
        public boolean queryCustomerInfo(int id, int customerID) throws InvalidTransactionException, RemoteException 
        {
                try{
                        synchronized (tList) {
                                if (tList.containsKey(id) == false) {  throw new InvalidTransactionException(id); }
                        }
                                        // Only need a shared lock on the customer
                                        boolean cusBool = false;
                                        cusBool = lm.Lock(id, Customer.getKey(customerID), LockManager.READ);
                                        
                                        return cusBool;
                        
                }
                catch (DeadlockException e) {
                	System.out.println ("Deadlock at transaction " + id + "when querying customer " + customerID);
                	abort(id); 
                	return false;
                }
              
        }
        
        public boolean itinerary(int id,int customer,Vector flightNumbers,String location,boolean car,boolean room) throws InvalidTransactionException, RemoteException 
        {
                try{
                        synchronized (tList) {
                                if (tList.containsKey(id) == false) {  throw new InvalidTransactionException(id); }
                        }
                                        // We need exclusive locks on the customer, hotel, car AND every flight in the vector
                                        boolean cusBool = false;
                                        boolean roomBool = false;
                                        boolean carBool = false;
                                        boolean flightBool = false;
                                        
                                        cusBool = lm.Lock(id, Customer.getKey(customer), LockManager.WRITE);
                                        
                                        if (car)
                                                carBool = lm.Lock (id, Car.getKey(location), LockManager.WRITE);
                                 
                                 if (room)
                                         roomBool = lm.Lock (id, Hotel.getKey(location), LockManager.WRITE);
                                        
                                        for (int i=0; i <flightNumbers.size(); i++)
                                 {
                                         int flightNum = Integer.parseInt((String)flightNumbers.get(i));
                                         flightBool = lm.Lock (id, Flight.getKey(flightNum), LockManager.WRITE);
                                 }
                                 return (cusBool && carBool && roomBool && flightBool);
                        
                }
                catch (DeadlockException e) {
                	System.out.println ("Deadlock at transaction " + id + "when making an itinerary");
                	abort(id);
                	return false;
                }
                
        }
}// end class

/*
* Automatically abort transactions that have reached their timeout.
*/
/*
class TransactionKicker implements Runnable {
        private TreeMap<Integer, Long> transactions;
        private TransactionManager manager;
	//private Middleware mid;
        
        public TransactionKicker(TransactionManager manager) {
                this.manager = manager;
                this.transactions = manager.tList;
		//this.mid = creator;
        }
        
        boolean running = true;
        public void run(){
                while (running) {
                        for (int t : transactions.keySet()) {
                                if (transactions.get(t) - System.currentTimeMillis() > TransactionManager.timeout) {
                                	try {
                                        manager.abort(t);
                                        transactions.remove(t);
                                	}
                                	catch (Exception er ) {
                                		System.err.println("Could not kick transaction " + t +"; it may already have been aborted!");
                                	}
                                }
                        }
                }
        }
}

class NonExistantTransactionException extends Exception {
        private static final long serialVersionUID = 7677682852151767675L;
        public NonExistantTransactionException(int tid) {
                super("Transaction " + tid + " is not a valid transaction. It may have been aborted due to timeout.");
        }
}*/

    

