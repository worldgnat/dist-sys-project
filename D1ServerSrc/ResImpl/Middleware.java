package ResImpl;

import ResInterface.*;

import java.util.*;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RMISecurityManager;



public class Middleware implements MiddlewareInt{



protected RMHashtable m_itemHT = new RMHashtable();    // The hastable will contain all the clients
protected static ResourceManager rmCars = null;        // RM for the cars
protected static ResourceManager rmRooms = null;       // RM for the rooms
protected static ResourceManager rmFlights = null;     // RM for the flights

public static void main(String args[]){


String server="localhost";
int port = 1099;

// The server names and ports for each RM
String serverCars="open-13.cs.mcgill.ca";
String serverRooms="open-10.cs.mcgill.ca";
String serverFlights="open-12.cs.mcgill.ca";
int portCars = 4030;
int portRooms = 1030;
int portFlights = 2030;


// Get the server and port for the rmi registry

if (args.length > 0)
        {
            server = args[0];
        }
        if (args.length > 1)
        {
            port = Integer.parseInt(args[1]);
        }
        if (args.length > 2)
        {
            System.out.println ("Usage: java Middleware [rmihost [rmiport]]");
            System.exit(1);
        }


try 
        {
	    Middleware obj = new Middleware();
	    MiddlewareInt mid = (MiddlewareInt) UnicastRemoteObject.exportObject(obj, 0);

		

            // get a reference to the rmiregistry
            Registry registry = LocateRegistry.getRegistry(server, port);
	        registry.rebind("middleware29",mid); // put the middleware remote object in the rmi registry for the client to see
	
	        
	    Registry registryFlights = LocateRegistry.getRegistry(serverFlights,portFlights);
	    Registry registryCars = LocateRegistry.getRegistry(serverCars,portCars);
	    Registry registryRooms = LocateRegistry.getRegistry(serverRooms,portRooms);
        // get the rms for each resource
	    rmFlights = (ResourceManager) registryFlights.lookup("flights29");
	    rmCars = (ResourceManager) registryCars.lookup("cars29");
	    rmRooms = (ResourceManager) registryRooms.lookup("rooms29");


            if( rmCars!=null && rmRooms!=null && rmFlights!=null)
            {
                System.out.println("Successful");
                System.out.println("Connected to RMs");
            }
            else
            {
                System.out.println("Unsuccessful");
            }
            // make call on remote method
        } 
catch (Exception e) 
        {    
            System.err.println("Middleware exception: " + e.toString());
            e.printStackTrace();
        }

	if (System.getSecurityManager() == null) {
		System.setSecurityManager(new RMISecurityManager());
	}
	

} // end main




public Middleware() throws RemoteException{ 
}




/***********************************
		FLIGHTS
***********************************/
// Just connect to the Flights RM, tell it to add the flight, and catch the exception
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
		throws RemoteException
	{
		 try 
		{
		    if(rmFlights!=null)
			{
			    rmFlights.addFlight(id,flightNum,flightSeats,flightPrice);
			    return true;
			}
		    else
			{
			    return false;
			}
		    // make call on remote method
		} 
	    catch (Exception e) 
		{	
		    System.err.println("Middleware exception: " + e.toString());
		    e.printStackTrace();
		    return false;
		}
		
	}


   public boolean deleteFlight(int id, int flightNum)
        throws RemoteException
    {
        return (rmFlights.deleteFlight(id, flightNum));
    }

   // Returns the number of empty seats on this flight
    public int queryFlight(int id, int flightNum)
        throws RemoteException
    {
        return (rmFlights.queryFlight(id, flightNum));
    }



 // Returns price of this flight
    public int queryFlightPrice(int id, int flightNum )
        throws RemoteException
    {
        return (rmFlights.queryFlightPrice(id,flightNum));
    }


    // Adds flight reservation to this customer.  
    public boolean reserveFlight(int id, int customerID, int flightNum)
        throws RemoteException
    {
    	boolean result = rmFlights.reserveFlight(id,customerID,flightNum);
    	if (result)
    	{
    		reserveItem(id, customerID, Flight.getKey(flightNum), String.valueOf(flightNum),queryFlightPrice(id,flightNum));
    		return result;
    	}
    	else {return result; }
    }


/*
    // Frees flight reservation record. Flight reservation records help us make sure we
    // don't delete a flight if one or more customers are holding reservations
    public boolean freeFlightReservation(int id, int flightNum)
        throws RemoteException
    {
        Trace.info("RM::freeFlightReservations(" + id + ", " + flightNum + ") called" );
        RMInteger numReservations = (RMInteger) readData( id, Flight.getNumReservationsKey(flightNum) );
        if ( numReservations != null ) {
            numReservations = new RMInteger( Math.max( 0, numReservations.getValue()-1) );
        } // if
        writeData(id, Flight.getNumReservationsKey(flightNum), numReservations );
        Trace.info("RM::freeFlightReservations(" + id + ", " + flightNum + ") succeeded, this flight now has "
                + numReservations + " reservations" );
        return true;
    }
 */



  // Returns the number of reservations for this flight. 
//    public int queryFlightReservations(int id, int flightNum)
//        throws RemoteException
//    {
//        Trace.info("RM::queryFlightReservations(" + id + ", #" + flightNum + ") called" );
//        RMInteger numReservations = (RMInteger) readData( id, Flight.getNumReservationsKey(flightNum) );
//        if ( numReservations == null ) {
//            numReservations = new RMInteger(0);
//        } // if
//        Trace.info("RM::queryFlightReservations(" + id + ", #" + flightNum + ") returns " + numReservations );
//        return numReservations.getValue();
//    }


   
/**************************************
		CARS
**************************************/

    // Just connect to the Cars RM, tell it to add the car and catch the exception
	public boolean addCars(int id, String location, int count, int price)
		throws RemoteException
	{
		 try 
		{
		    if(rmCars!=null)
			{
			    rmCars.addCars(id,location,count,price);
			    return true;
			}
		    else
			{
			    return false;
			}
		    // make call on remote method
		} 

		catch (Exception e) 
		{	
		    System.err.println("Middleware exception: " + e.toString());
		    e.printStackTrace();
		    return false;
		}
	}


    // Delete cars from a location
    public boolean deleteCars(int id, String location)
        throws RemoteException
    {
        return (rmCars.deleteCars(id,location));
    }

    // Returns the number of cars available at a location
    public int queryCars(int id, String location)
        throws RemoteException
    {
        return (rmCars.queryCars(id,location));
    }


    // Returns price of cars at this location
    public int queryCarsPrice(int id, String location)
        throws RemoteException
    {
        return (rmCars.queryCarsPrice(id,location));
    }


     public boolean reserveCar(int id, int customerID, String location)
        throws RemoteException
    {
    	boolean result = rmCars.reserveCar(id,customerID,location);
    	if (result)
    	{
    		reserveItem(id, customerID, Car.getKey(location), location,queryCarsPrice(id,location));
    		return result;
    	}
    	else { return result; }
    }

/*************************************
		ROOMS
*************************************/

// Just connect to the Rooms RM, tell it to add the room, and catch the exception
public boolean addRooms(int id, String location, int count, int price)
        throws RemoteException
    {
     	 try 
		{
		    if(rmRooms!=null)
			{
			    rmRooms.addRooms(id,location,count,price);
			    return true;
			}
		    else
			{
			    return false;
			}
		    // make call on remote method
		} 

		catch (Exception e) 
		{	
		    System.err.println("Middleware exception: " + e.toString());
		    e.printStackTrace();
		    return false;
		}
      
    }


 // Delete rooms from a location
    public boolean deleteRooms(int id, String location)
        throws RemoteException
    {
        return (rmRooms.deleteRooms(id, location));
        
    }

// Returns the number of rooms available at a location
    public int queryRooms(int id, String location)
        throws RemoteException
    {
        return (rmRooms.queryRooms(id,location));
    }


    
    
    // Returns room price at this location
    public int queryRoomsPrice(int id, String location)
        throws RemoteException
    {
        return (rmRooms.queryRoomsPrice(id,location));
    }


   // Adds room reservation to this customer. 
    public boolean reserveRoom(int id, int customerID, String location)
        throws RemoteException
    {
    	boolean result = rmRooms.reserveRoom(id,customerID,location);
    	if (result)
    	{
    		reserveItem(id, customerID, Hotel.getKey(location), location,queryRoomsPrice(id,location));
    		return result;
    	}
    	else
        { return result; }
    }


   


    


/***************************************
		CUSTOMERS
	(same code as in ResourceManager)
***************************************/



 public int newCustomer(int id)
        throws RemoteException
    {
        Trace.info("INFO: RM::newCustomer(" + id + ") called" );
        // Generate a globally unique ID for the new customer
        int cid = Integer.parseInt( String.valueOf(id) +
                                String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                                String.valueOf( Math.round( Math.random() * 100 + 1 )));
        Customer cust = new Customer( cid );
        writeData( id, cust.getKey(), cust );
        Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid );
        
        /***************************************/
        // Make the other RMs add a new customer
        rmCars.newCustomer(id,cid);
        rmRooms.newCustomer(id,cid);
        rmFlights.newCustomer(id,cid);
        /*********************************/
        
        return cid;
    }




    // I opted to pass in customerID instead. This makes testing easier
    public boolean newCustomer(int id, int customerID )
        throws RemoteException
    {
        Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if ( cust == null ) {
            cust = new Customer(customerID);
            writeData( id, cust.getKey(), cust );
            Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") created a new customer" );
            
            /***************************************/
            // Make the other RMs add a new customer
            rmCars.newCustomer(id,customerID);
            rmRooms.newCustomer(id,customerID);
            rmFlights.newCustomer(id,customerID);
            /*********************************/
            
            return true;
        } else {
            Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") failed--customer already exists");
            return false;
        } // else
    }



    // Deletes customer from the database. 
    public boolean deleteCustomer(int id, int customerID)
        throws RemoteException
    {
        Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if ( cust == null ) {
            Trace.warn("RM::deleteCustomer(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            return false;
        } else {
        	
        	/***************************************/
        	rmCars.deleteCustomer(id,customerID);
        	rmRooms.deleteCustomer(id,customerID);
        	rmFlights.deleteCustomer(id,customerID);
        	/*************************************/
            
            // remove the customer from the storage
            removeData(id, cust.getKey());
            
            Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") succeeded" );
            return true;
        } // if
    }






    // Returns data structure containing customer reservation info. Returns null if the
    //  customer doesn't exist. Returns empty RMHashtable if customer exists but has no
    //  reservations.
    public RMHashtable getCustomerReservations(int id, int customerID)
        throws RemoteException
    {
        Trace.info("RM::getCustomerReservations(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if ( cust == null ) {
            Trace.warn("RM::getCustomerReservations failed(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            return null;
        } else {
            return cust.getReservations();
        } // if
    }





    // return a bill
    public String queryCustomerInfo(int id, int customerID)
        throws RemoteException
    {
        Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if ( cust == null ) {
            Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            return "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
        } else {
                String s = cust.printBill();
                Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
                System.out.println( s );
                return s;
        } // if
    }


/***************************************
		MISC
	(same as in ResourceManager)
***************************************/

	// Reads a data item
	private RMItem readData( int id, String key )
	{
		synchronized(m_itemHT){
			return (RMItem) m_itemHT.get(key);
		}
	}

	// Writes a data item
	private void writeData( int id, String key, RMItem value )
	{
		synchronized(m_itemHT){
			m_itemHT.put(key, value);
		}
	}
	
	// Remove the item out of storage
	protected RMItem removeData(int id, String key){
		synchronized(m_itemHT){
			return (RMItem)m_itemHT.remove(key);
		}
	}


	// deletes the entire item
	protected synchronized boolean deleteItem(int id, String key)
	{
		Trace.info("RM::deleteItem(" + id + ", " + key + ") called" );
		ReservableItem curObj = (ReservableItem) readData( id, key );
		// Check if there is such an item in the storage
		if( curObj == null ) {
			Trace.warn("RM::deleteItem(" + id + ", " + key + ") failed--item doesn't exist" );
			return false;
		} else {
			if(curObj.getReserved()==0){
				removeData(id, curObj.getKey());
				Trace.info("RM::deleteItem(" + id + ", " + key + ") item deleted" );
				return true;
			}
			else{
				Trace.info("RM::deleteItem(" + id + ", " + key + ") item can't be deleted because some customers reserved it" );
				return false;
			}
		} // if
	}
	

	// query the number of available seats/rooms/cars
	protected int queryNum(int id, String key) {
		Trace.info("RM::queryNum(" + id + ", " + key + ") called" );
		ReservableItem curObj = (ReservableItem) readData( id, key);
		int value = 0;  
		if( curObj != null ) {
			value = curObj.getCount();
		} // else
		Trace.info("RM::queryNum(" + id + ", " + key + ") returns count=" + value);
		return value;
	}	
	
	// query the price of an item
	protected int queryPrice(int id, String key){
		Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") called" );
		ReservableItem curObj = (ReservableItem) readData( id, key);
		int value = 0; 
		if( curObj != null ) {
			value = curObj.getPrice();
		} // else
		Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") returns cost=$" + value );
		return value;		
	}



        // reserve an item
	protected synchronized boolean reserveItem(int id, int customerID, String key, String location, int price){
		Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );		
		// Read customer object if it exists (and read lock it)
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );		
		if( cust == null ) {
			Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
			return false;
		} 
		else{
			cust.reserve(key,location,price);
			writeData(id,cust.getKey(),cust);
			return true;
		}
		
	}




// Reserve an itinerary 
    public boolean itinerary(int id,int customer,Vector flightNumbers,String location,boolean car,boolean room)
        throws RemoteException
    {
    	boolean carBool = true;
    	boolean roomBool = true;
    	boolean flightBool = true;
    	
    	if (car==true)
    	{
            carBool = reserveCar(id,customer,location);
    	}
    	
    	if (room==true)
    	{
            roomBool = reserveRoom(id,customer,location);
    	}
    	
    	// Reserve all the flights from the vector
    	for (int i=0; i <flightNumbers.size(); i++)
    	{ 
    		int flightNum = Integer.parseInt((String)flightNumbers.get(i));
            flightBool = reserveFlight(id,customer,flightNum); 
    	}
    	
    	if (carBool == true && roomBool == true && flightBool == true)
    	{ return true; }
    	
    	else if (carBool == false)
    	{
    		Trace.warn("Invalid car reservation");
    		return false;
    	}
    	
    	else if (roomBool == false)
    	{
    		Trace.warn("Invalid room reservation");
    		return false;
    	}
    	
    	else
    	{
    		Trace.warn("Invalid flight reservation");
    		return false;
    	}
    }






    
/*************************************
		END
*************************************/
    
   

}
