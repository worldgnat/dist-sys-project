package ResInterface;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

import exceptions.*;

public interface MiddlewareInt extends Remote
{
	
	public boolean shutdown() throws RemoteException;
        /*
         * Commits the given transaction on this RM.
         */
	public void start(int id) throws RemoteException;
        public void commit(int tid) throws RemoteException,InvalidTransactionException,TransactionAbortedException;
        
        /*
         * Aborts the given transaction on this RM.
         */
        public void abort(int tid) throws RemoteException, InvalidTransactionException;
        
        /* reserve a car at this location */
    public boolean reserveCar(int id, int customer, String location)
        throws RemoteException, InvalidTransactionException;
         /* return the price of a car at a location */
    public int queryCarsPrice(int id, String location)
        throws RemoteException, InvalidTransactionException;

        /* return the number of cars available at a location */
    public int queryCars(int id, String location)
        throws RemoteException, InvalidTransactionException;

         /* Delete all Cars from a location.
* It may not succeed if there are reservations for this location
*
* @return success
*/                
    public boolean deleteCars(int id, String location)
        throws RemoteException, InvalidTransactionException;

        /* Add cars to a location.
* This should look a lot like addFlight, only keyed on a string location
* instead of a flight number.
*/
        public boolean addCars(int id, String location, int numCars, int price)
        throws RemoteException, InvalidTransactionException;
   
    
    /* Add seats to a flight. In general this will be used to create a new
* flight, but it should be possible to add seats to an existing flight.
* Adding to an existing flight should overwrite the current price of the
* available seats.
*
* @return success.
*/

    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
        throws RemoteException, InvalidTransactionException;
   
    
    
/* Add rooms to a location.
* This should look a lot like addFlight, only keyed on a string location
* instead of a flight number.
*/
    public boolean addRooms(int id, String location, int numRooms, int price)
        throws RemoteException, InvalidTransactionException;                         

                        
    /* new customer just returns a unique customer identifier */
    public int newCustomer(int id)
        throws RemoteException, InvalidTransactionException;
    
    /* new customer with providing id */
    public boolean newCustomer(int id, int cid)
    throws RemoteException, InvalidTransactionException;

    /**
* Delete the entire flight.
* deleteflight implies whole deletion of the flight.
* all seats, all reservations. If there is a reservation on the flight,
* then the flight cannot be deleted
*
* @return success.
     * @throws InvalidTransactionException 
*/
    public boolean deleteFlight(int id, int flightNum)
        throws RemoteException, InvalidTransactionException;
    
    /* Delete all Rooms from a location.
* It may not succeed if there are reservations for this location.
*
* @return success
*/
    public boolean deleteRooms(int id, String location)
        throws RemoteException, InvalidTransactionException;
    
    /* deleteCustomer removes the customer and associated reservations */
    public boolean deleteCustomer(int id,int customer)
        throws RemoteException, InvalidTransactionException;

    /* queryFlight returns the number of empty seats. */
    public int queryFlight(int id, int flightNumber)
        throws RemoteException, InvalidTransactionException;

    

    /* return the number of rooms available at a location */
    public int queryRooms(int id, String location)
        throws RemoteException, InvalidTransactionException;

    /* return a bill */
    public String queryCustomerInfo(int id,int customer)
        throws RemoteException, InvalidTransactionException;
    
    /* queryFlightPrice returns the price of a seat on this flight. */
    public int queryFlightPrice(int id, int flightNumber)
        throws RemoteException, InvalidTransactionException;

   

    /* return the price of a room at a location */
    public int queryRoomsPrice(int id, String location)
        throws RemoteException, InvalidTransactionException;

    /* Reserve a seat on this flight*/
   public boolean reserveFlight(int id, int customer, int flightNumber)
        throws RemoteException, InvalidTransactionException;

    

    /* reserve a room certain at this location */
    public boolean reserveRoom(int id, int customer, String locationd)
        throws RemoteException, InvalidTransactionException;


    /* reserve an itinerary */
    public boolean itinerary(int id,int customer,Vector flightNumbers,String location, boolean Car, boolean Room)
        throws RemoteException, InvalidTransactionException;

}
