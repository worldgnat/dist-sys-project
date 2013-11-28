package ResImpl;

import exceptions.InvalidTransactionException;
import exceptions.TransactionAbortedException;
import groupComm.AbortMessage;
import groupComm.CommitMessage;
import groupComm.HashtableUpdate;
import groupComm.ImThePrimary;
import groupComm.StartMessage;

import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Calendar;
import java.util.Queue;
import java.util.TreeMap;
import java.util.Vector;

import ResInterface.MiddleResourceManageInt;
import ResInterface.MiddlewareInt;
import ResInterface.ResourceManager;


public class Middleware implements MiddlewareInt, MiddleResourceManageInt {
	protected RMHashtable m_itemHT = new RMHashtable(); // The hastable will contain all the clients
	protected static ResourceManager rmCars = null; // RM for the cars
	protected static ResourceManager rmRooms = null; // RM for the rooms
	protected static ResourceManager rmFlights = null; // RM for the flights
	protected TransactionManager tM;
	protected TreeMap<Integer, TemporaryHT> openTransactions = new TreeMap<Integer, TemporaryHT>();
	GroupManagement gm;
	int port;
	String binding;
	
	//These are the names of the RM JGroups.
	final static String flightsChannel = "flights29";
	final static String roomsChannel = "rooms29";
	final static String carsChannel = "cars29";

	public static void main(String args[]) {
		String server="localhost";
		int port = 1099;

		// Get the server and port for the rmi registry
		if (args.length > 0)
		{
			port = Integer.parseInt(args[0]);
		}
		else
		{
			System.out.println ("Usage: java Middleware rmiport");
			System.exit(1);
		}

		try
		{
			Middleware obj = new Middleware(port, "middleware29");
			MiddlewareInt mid = (MiddlewareInt) UnicastRemoteObject.exportObject(obj, 0);

			// get a reference to the rmiregistry
			Registry registry = LocateRegistry.getRegistry(server, port);
			registry.rebind("middleware29",mid); // put the middleware remote object in the rmi registry for the client to see

			//Set up the Middleware group
			obj.setGM(new GroupManagement(obj, "middleware29"));
			
			if( rmCars!=null && rmRooms!=null && rmFlights!=null)
			{
				System.out.println("Successful");
				System.out.println("Connected to RMs");
			}
			else
			{
				System.out.println("Unsuccessful (maybe). Or maybe we're waiting for responses from the primaries.");
			}
			// make call on remote method
		}
		catch (Exception e)
		{
			System.err.println("Middleware exception: " + e.toString());
			e.printStackTrace();
		}

		if (System.getSecurityManager() == null) {
			//System.setSecurityManager(new RMISecurityManager());
		}


	} // end main

	public Middleware(int port, String binding) throws RemoteException{
		tM = new ResImpl.TransactionManager(this);
		this.port = port;
		this.binding = binding;
	}
	
	public int getPort() { return port; }
	public String getBinding() { return binding; }
	
	public void setGM(GroupManagement gm) {
		this.gm = gm;
	}
	public void setPrimary(String hostname, int port, String type) { 
		try {
			switch (type) {
			case "flights29":
				rmFlights = bindRM(hostname, port, type);
				break;
			case "rooms29":
				rmRooms = bindRM(hostname, port, type);
				break;
			case "cars29":
				rmCars = bindRM(hostname, port, type);
				break;
			}
			System.out.println("Received notification from " + hostname + " that it is the primary of " + type + " on port " + port);
		}
		catch (Exception er) {
			Trace.warn("MIDDLEWARE: Failed to bind new primary RM.");
		}
	}
	
	/*
	 * This makes binding RMs easier
	 */
	public static ResourceManager bindRM(String server, int port, String binding) throws RemoteException, NotBoundException {
		Registry registryFlights = LocateRegistry.getRegistry(server,port);
		System.out.println("Bound new registry " + binding);
		return (ResourceManager) registryFlights.lookup(binding);
	}
	/*
	public void findPrimary(ResourceManager rm, String channel) {
		ImThePrimary primary = gm.findPrimary(channel);
		try {
			rm = bindRM(primary.getHostname(), primary.getPort(), channel);
		}
		catch (Exception er) {
			Trace.error("[Middleware] - Could not rebind to primary " + channel +" rm.");
			er.printStackTrace();
		}
	}*/

	public boolean shutdown() throws RemoteException{
		rmCars.shutdown();
		rmFlights.shutdown();
		rmRooms.shutdown();
		return true;
	}
	// Reads a data item
	private RMItem readData( int id, String key )
	{
		/*
		 * It seems rash to lock all of openTransactions for this, but what someone deletes the transaction?
		 */
		synchronized(openTransactions) {
			if (openTransactions.get(id).containsKey(key)) {
				return (RMItem)openTransactions.get(id).get(key);
			}
		}
		synchronized(m_itemHT) {
			return (RMItem) m_itemHT.get(key);
		}
	}

	// Writes a data item
	public void writeData(int id, String key, RMItem value)
	{
		synchronized(openTransactions) {
			// m_itemHT.put(key, value);
			if (openTransactions.containsKey(id)) {
				//If the transaction is already opened, enqueue this update
				openTransactions.get(id).put(key, value);
			}
			else { //Otherwise, create a queue for the transaction, enqueue the update, and put the transaction in the openTransactions tree.
				openTransactions.put(id, new TemporaryHT());
				openTransactions.get(id).put(key, value);
			}
		}
		//Write the changes to the backups
		gm.sendUpdates(new HashtableUpdate(id, key, value, getBinding()));
	}

	// Remove the item out of storage
	public RMItem removeData(int id, String key) {
		synchronized(m_itemHT) {
			synchronized(openTransactions) {
				RMItem temp = (RMItem)openTransactions.get(id).remove(key);
				if (temp == null) { //The value was not stored in the temporary hashtable, but in m_itemHT
					Trace.info("MID::item was not in the temporary hashtable.");
					gm.sendUpdates(new HashtableUpdate(id, key, null, getBinding()));
					return (RMItem)m_itemHT.get(key);
				}
				// The value is in the temp HT
				
				else{
				 gm.sendUpdates(new HashtableUpdate(id,key,null, getBinding()));
				 return temp;
				}
			}
		}
	}
	
	public void start(int tid) throws RemoteException{
		synchronized (openTransactions) {
			if (openTransactions.containsKey(tid) == false)
			{
				tM.start(tid);
				try {
					rmFlights.start(tid);
				}
				catch(ConnectException er) {
					System.out.println("Cannot connect to Flights RM");
					//findPrimary(rmFlights, flightsChannel);
				}
				try {
					rmCars.start(tid);
				}
				catch (ConnectException er) {
					System.out.println("Cannot connect to Cars RM");
					//findPrimary(rmCars, carsChannel);
				}
				try {
					rmRooms.start(tid);
				}
				catch (ConnectException er) {
					System.out.println("Cannot connect to Rooms RM");
					//findPrimary(rmRooms, roomsChannel);
				}
				openTransactions.put(tid, new TemporaryHT());
				System.out.println("Started transaction " + tid);
				//Start the transaction on the backups.
				gm.sendUpdates(new StartMessage(tid, getBinding()));
			}
		}
	}

	public void commit(int tid) throws RemoteException, InvalidTransactionException, TransactionAbortedException{
		tM.commit(tid);
		rmFlights.commit(tid);
		rmCars.commit(tid);
		rmRooms.commit(tid);

		synchronized (openTransactions){
			if (openTransactions.containsKey(tid))
			{
				Queue<Object[]> queue = openTransactions.get(tid).changeQueue;
				synchronized (m_itemHT){
					Object[] values;
					while(!queue.isEmpty()){
						values = queue.remove();
						if (values.length == 4) {
							int customerID = (int)values[0];
							Customer cust = (Customer) readData(tid, Customer.getKey(customerID) );      
							String key = (String)values[1];
							String location = (String)values[2];
							int price = (int)values[3];
							if( cust == null ) {
								Trace.warn("Middleware::write customer( " + tid + ", " + customerID + ", " + key + ", "+location+") failed--customer doesn't exist" );
							}
							else{
								cust.reserve(key,location,price);
								m_itemHT.put(cust.getKey(), cust);
							}
						}
						else if (values[1] != null) {
							m_itemHT.put(values[0], (RMItem)values[1]); 
						}
						else
						{ m_itemHT.remove(values[0]);}
					}
				}

				openTransactions.remove(tid);
				System.out.println("COmmitted transaction " + tid);
				//Commit the transaction on the backups.
				gm.sendUpdates(new CommitMessage(tid, getBinding()));
			}

			else
				throw new InvalidTransactionException(tid);
		}
	}
	public void abort(int tid) throws RemoteException, InvalidTransactionException{
		tM.abort(tid);
		//tmAbort(tid);
	}

	public void tmAbort (int tid) throws RemoteException, InvalidTransactionException{
		rmFlights.abort(tid);
		rmCars.abort(tid);
		rmRooms.abort(tid);
		synchronized(openTransactions){
			if (openTransactions.containsKey(tid)){
				openTransactions.remove(tid);
				//Abort this transaction on the backups.
				gm.sendUpdates(new AbortMessage(tid, getBinding()));
			}
			else
				throw new InvalidTransactionException(tid);
		}
		System.out.println("Aborted transaction " + tid);
	}


	/***********************************
                FLIGHTS
	 ***********************************/
	// Just connect to the Flights RM, tell it to add the flight, and catch the exception
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
			throws RemoteException, InvalidTransactionException
			{
		synchronized (openTransactions){
			if (openTransactions.containsKey(id)==false){ throw new InvalidTransactionException(id); }
		}
		if(rmFlights!=null && tM.addFlight(id,flightNum,flightSeats,flightPrice) == true)
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




	public boolean deleteFlight(int id, int flightNum)
			throws RemoteException, InvalidTransactionException
			{
		synchronized(openTransactions){
			if (openTransactions.containsKey(id) == false){ throw new InvalidTransactionException(id); }
		}
		if (tM.deleteFlight(id,flightNum))
			return (rmFlights.deleteFlight(id, flightNum));
		else
		{
			return false;
		}
			}

	// Returns the number of empty seats on this flight
	public int queryFlight(int id, int flightNum)
			throws RemoteException, InvalidTransactionException
			{
		synchronized(openTransactions){
			if (openTransactions.containsKey(id) == false){ throw new InvalidTransactionException(id); }
		}
		if (tM.queryFlight(id,flightNum))
			return (rmFlights.queryFlight(id, flightNum));
		else
			return -1;
			}



	// Returns price of this flight
	public int queryFlightPrice(int id, int flightNum )
			throws RemoteException, InvalidTransactionException
			{
		synchronized(openTransactions){
			if (openTransactions.containsKey(id) == false) { throw new InvalidTransactionException(id); }
		}
		if (tM.queryFlightPrice(id,flightNum))
			return (rmFlights.queryFlightPrice(id,flightNum));
		else
			return -1;
			}


	// Adds flight reservation to this customer.
	public boolean reserveFlight(int id, int customerID, int flightNum)
			throws RemoteException, InvalidTransactionException
			{
		synchronized(openTransactions){
			if (openTransactions.containsKey(id) == false ){ throw new InvalidTransactionException(id); }
		}
		if (tM.reserveFlight(id, customerID, flightNum)) {
			boolean result = rmFlights.reserveFlight(id,customerID,flightNum);
			if (result)
			{
				reserveItem(id, customerID, Flight.getKey(flightNum), String.valueOf(flightNum), queryFlightPrice(id,flightNum));
				return result;
			}
			else {return result; }
		}
		else return false;

			}


	/*
// Frees flight reservation record. Flight reservation records help us make sure we
// don't delete a flight if one or more customers are holding reservations
public boolean freeFlightReservation(int id, int flightNum)
throws RemoteException
{
Trace.info("Middleware::freeFlightReservations(" + id + ", " + flightNum + ") called" );
RMInteger numReservations = (RMInteger) readData( id, Flight.getNumReservationsKey(flightNum) );
if ( numReservations != null ) {
numReservations = new RMInteger( Math.max( 0, numReservations.getValue()-1) );
} // if
writeData(id, Flight.getNumReservationsKey(flightNum), numReservations );
Trace.info("Middleware::freeFlightReservations(" + id + ", " + flightNum + ") succeeded, this flight now has "
+ numReservations + " reservations" );
return true;
}
	 */



	// Returns the number of reservations for this flight.
	// public int queryFlightReservations(int id, int flightNum)
	// throws RemoteException
	// {
	// Trace.info("Middleware::queryFlightReservations(" + id + ", #" + flightNum + ") called" );
	// RMInteger numReservations = (RMInteger) readData( id, Flight.getNumReservationsKey(flightNum) );
	// if ( numReservations == null ) {
	// numReservations = new RMInteger(0);
	// } // if
	// Trace.info("Middleware::queryFlightReservations(" + id + ", #" + flightNum + ") returns " + numReservations );
	// return numReservations.getValue();
	// }



	/**************************************
                CARS

	 **************************************/

	// Just connect to the Cars RM, tell it to add the car and catch the exception
	public boolean addCars(int id, String location, int count, int price)
			throws RemoteException, InvalidTransactionException
			{
		synchronized(openTransactions){
			if (openTransactions.containsKey(id) == false){ throw new InvalidTransactionException(id); }
		}

		if(rmCars!=null && tM.addCars(id,location,count,price) == true)
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
	
    // Delete cars from a location
    public boolean deleteCars(int id, String location)
    throws RemoteException, InvalidTransactionException
    {
		synchronized(openTransactions){
			if (openTransactions.containsKey(id) == false){ throw new InvalidTransactionException(id); }
		}
				if (tM.deleteCars(id,location))
           				 return (rmCars.deleteCars(id,location));
				else
				return false;
			
		
   }

	// Returns the number of cars available at a location
	public int queryCars(int id, String location)
			throws RemoteException, InvalidTransactionException
			{
		synchronized(openTransactions){
			if (openTransactions.containsKey(id) == false) { throw new InvalidTransactionException(id); }
		}
		if (tM.queryCars(id,location))
		{return (rmCars.queryCars(id,location));}
		else
			return -1;
			}


	// Returns price of cars at this location
	public int queryCarsPrice(int id, String location)
			throws RemoteException, InvalidTransactionException
			{
		synchronized(openTransactions){
			if (openTransactions.containsKey(id) == false){ throw new InvalidTransactionException(id); }
		}
		if (tM.queryCarsPrice(id,location))
		{return (rmCars.queryCarsPrice(id,location));}
		else
			return -1;
			}


	public boolean reserveCar(int id, int customerID, String location)
			throws RemoteException, InvalidTransactionException
			{
		synchronized(openTransactions){
			if (openTransactions.containsKey(id) == false){ throw new InvalidTransactionException(id); }
		}

		if (tM.reserveCar(id,customerID,location))
		{
			synchronized(openTransactions){
				if (openTransactions.containsKey(id)){
					if (tM.reserveCar(id,customerID,location))
					{
						boolean result = rmCars.reserveCar(id,customerID,location);
						if (result)
						{
							reserveItem(id, customerID, Car.getKey(location), location, queryCarsPrice(id, location));
							return result;
						}
						else { return result; }
					}
					else
					{return false;}
				}
				else return false;
			}
		}
		return false;

			}

	/*************************************
                ROOMS
	 *************************************/

	// Just connect to the Rooms RM, tell it to add the room, and catch the exception
	public boolean addRooms(int id, String location, int count, int price)
			throws RemoteException, InvalidTransactionException
			{
		synchronized(openTransactions){
			if (openTransactions.containsKey(id) == false) { throw new InvalidTransactionException(id); }
		}

		if(rmRooms!=null && tM.addRooms(id,location,count,price) == true)
		{
			rmRooms.addRooms(id,location,count,price);
			return true;
		}
		else
		{
			return false;
		}

			}
	
	// Delete rooms from a location
    public boolean deleteRooms(int id, String location)
    throws RemoteException, InvalidTransactionException
    {
		synchronized(openTransactions){
			if (openTransactions.containsKey(id) == false){ throw new InvalidTransactionException(id); }
		}
			if (tM.deleteRooms(id,location))
            			return (rmRooms.deleteRooms(id, location));
			else
				return false;

    }

	// Returns the number of rooms available at a location
	public int queryRooms(int id, String location)
			throws RemoteException, InvalidTransactionException
			{
		synchronized(openTransactions){
			if (openTransactions.containsKey(id) == false){ throw new InvalidTransactionException(id); }
		}
		if (tM.queryRooms(id,location))
			return (rmRooms.queryRooms(id,location));
		else
			return -1;
			}

	// Returns room price at this location
	public int queryRoomsPrice(int id, String location)
			throws RemoteException, InvalidTransactionException
			{
		synchronized(openTransactions){
			if (openTransactions.containsKey(id) == false){ throw new InvalidTransactionException(id); }
		}
		if (tM.queryRoomsPrice(id,location))
			return (rmRooms.queryRoomsPrice(id,location));
		else
			return -1;
			}


	// Adds room reservation to this customer.
	public boolean reserveRoom(int id, int customerID, String location)
			throws RemoteException, InvalidTransactionException
			{
		synchronized(openTransactions){
			if (openTransactions.containsKey(id)==false){ throw new InvalidTransactionException(id); }
		}
		if (tM.reserveRoom(id,customerID,location))
		{
			boolean result = rmRooms.reserveRoom(id,customerID,location);
			if (result)
			{
				reserveItem(id, customerID, Hotel.getKey(location), location, queryRoomsPrice(id, location));
				return result;
			}
			else
			{ return result; }
		}
		else
			return false;

			}

	/***************************************
                CUSTOMERS
        (same code as in ResourceManager)
	 ***************************************/

	public int newCustomer(int id)
			throws RemoteException, InvalidTransactionException
			{
		synchronized(openTransactions){
			if (openTransactions.containsKey(id) == false){ throw new InvalidTransactionException(id); }
		}
		Trace.info("INFO: Middleware::newCustomer(" + id + ") called" );
		// Generate a globally unique ID for the new customer
		int cid = Integer.parseInt( String.valueOf(id) +
				String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
				String.valueOf( Math.round( Math.random() * 100 + 1 )));
		if (tM.newCustomer(id,cid))
		{
			Customer cust = new Customer( cid );
			writeData( id, cust.getKey(), cust );
			Trace.info("Middleware::newCustomer(" + cid + ") returns ID=" + cid );

			return cid;
		}
		else
			throw new InvalidTransactionException(id);
			}




	// I opted to pass in customerID instead. This makes testing easier
	public boolean newCustomer(int id, int customerID )
			throws RemoteException, InvalidTransactionException
			{
		synchronized(openTransactions){
			if (openTransactions.containsKey(id) == false){ throw new InvalidTransactionException(id); }
		}
		Trace.info("INFO: Middleware::newCustomer(" + id + ", " + customerID + ") called" );
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
		if ( cust == null && tM.newCustomer(id,customerID)) {
			cust = new Customer(customerID);
			writeData( id, cust.getKey(), cust );
			Trace.info("INFO: Middleware::newCustomer(" + id + ", " + customerID + ") created a new customer" );

			/***************************************/
			// Make the other RMs add a new customer
			rmCars.newCustomer(id,customerID);
			rmRooms.newCustomer(id,customerID);
			rmFlights.newCustomer(id,customerID);
			/*********************************/

			return true;
		} else {
			Trace.info("INFO: Middleware::newCustomer(" + id + ", " + customerID + ") failed--customer already exists");
			return false;
		} // else

			}



	// Deletes customer from the database.
	public boolean deleteCustomer(int id, int customerID)
			throws RemoteException, InvalidTransactionException
			{
		synchronized(openTransactions){
			if (openTransactions.containsKey(id) == false){ throw new InvalidTransactionException(id); }
		}
		Trace.info("Middleware::deleteCustomer(" + id + ", " + customerID + ") called" );
		if(tM.deleteCustomer(id,customerID))
		{
			Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
			if ( cust == null ) {
				Trace.warn("Middleware::deleteCustomer(" + id + ", " + customerID + ") failed--customer doesn't exist" );
				return false;
			} else {
				Trace.info("Middleware::deleteCustomer(" + id + ", " + customerID + ") succeeded" );
				return true;
			} // if
		}

		else
			return false;

			}
	// Returns data structure containing customer reservation info. Returns null if the
	// customer doesn't exist. Returns empty RMHashtable if customer exists but has no
	// reservations.
	public RMHashtable getCustomerReservations(int id, int customerID)
			throws RemoteException
			{
		Trace.info("Middleware::getCustomerReservations(" + id + ", " + customerID + ") called" );
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
		if ( cust == null ) {
			Trace.warn("Middleware::getCustomerReservations failed(" + id + ", " + customerID + ") failed--customer doesn't exist" );
			return null;
		} else {
			return cust.getReservations();
		} // if
			}

	// return a bill
	public String queryCustomerInfo(int id, int customerID)
			throws RemoteException, InvalidTransactionException
			{
		synchronized(openTransactions){
			if (openTransactions.containsKey(id) == false){ throw new InvalidTransactionException(id); }
		}
		Trace.info("Middleware::queryCustomerInfo(" + id + ", " + customerID + ") called" );
		if(tM.queryCustomerInfo(id,customerID))
		{
			Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
			if ( cust == null ) {
				Trace.warn("Middleware::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
				return ""; // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
			} else {
				String s = cust.printBill();
				Trace.info("Middleware::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
				System.out.println( s );
				return s;
			} // if
		}
		else
			return "";
			}


	/***************************************
                MISC
        (same as in ResourceManager)
	 ***************************************/

	// deletes the entire item
	protected synchronized boolean deleteItem(int id, String key)
	{
		Trace.info("Middleware::deleteItem(" + id + ", " + key + ") called" );
		ReservableItem curObj = (ReservableItem) readData( id, key );
		// Check if there is such an item in the storage
		if( curObj == null ) {
			Trace.warn("Middleware::deleteItem(" + id + ", " + key + ") failed--item doesn't exist" );
			return false;
		} else {
			if(curObj.getReserved()==0){
				removeData(id, curObj.getKey());
				Trace.info("Middleware::deleteItem(" + id + ", " + key + ") item deleted" );
				return true;
			}
			else{
				Trace.info("Middleware::deleteItem(" + id + ", " + key + ") item can't be deleted because some customers reserved it" );
				return false;
			}
		} // if
	}


	// query the number of available seats/rooms/cars
	protected int queryNum(int id, String key) {
		Trace.info("Middleware::queryNum(" + id + ", " + key + ") called" );
		ReservableItem curObj = (ReservableItem) readData( id, key);
		int value = 0;
		if( curObj != null ) {
			value = curObj.getCount();
		} // else
		Trace.info("Middleware::queryNum(" + id + ", " + key + ") returns count=" + value);
		return value;
	}        

	// query the price of an item
	protected int queryPrice(int id, String key){
		Trace.info("Middleware::queryCarsPrice(" + id + ", " + key + ") called" );
		ReservableItem curObj = (ReservableItem) readData( id, key);
		int value = 0;
		if( curObj != null ) {
			value = curObj.getPrice();
		} // else
		Trace.info("Middleware::queryCarsPrice(" + id + ", " + key + ") returns cost=$" + value );
		return value;                
	}



	// reserve an item
	protected synchronized boolean reserveItem(int id, int customerID, String key, String location, int price) {
		Trace.info("Middleware::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );
		Customer cust = (Customer)readData(id, Customer.getKey(customerID)).clone();
		cust.reserve(key, location, price);
		writeData(id, cust.getKey(), cust);
		Trace.info("Middleware::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
		return true;
	}




	// Reserve an itinerary
	public boolean itinerary(int id,int customer,Vector flightNumbers,String location,boolean car,boolean room)
			throws RemoteException, InvalidTransactionException
			{
		boolean carBool = true;
		boolean roomBool = true;
		boolean flightBool = true;

		if (car==true)
		{
			carBool = reserveCar(id,customer,location);

			if (room==true)
			{
				roomBool = reserveRoom(id,customer,location);

				for (int i = 0; i < flightNumbers.size(); i++)
				{
					int flightNum = Integer.parseInt((String)flightNumbers.get(i));
					flightBool = reserveFlight(id,customer,flightNum);	
				}
			}

			else
			{
				for (int i = 0; i < flightNumbers.size(); i++)
				{
					int flightNum = Integer.parseInt((String)flightNumbers.get(i));
					flightBool = reserveFlight(id,customer,flightNum);
				}
			}
		}


		else
		{
			if (room == true)
			{
				roomBool = reserveRoom(id,customer,location);

				for (int i = 0; i < flightNumbers.size(); i++)
				{
					int flightNum = Integer.parseInt((String)flightNumbers.get(i));
					flightBool = reserveFlight(id,customer,flightNum);
				}
			}

			else
			{
				for (int i = 0; i < flightNumbers.size(); i++)
				{
					int flightNum = Integer.parseInt((String)flightNumbers.get(i));
					flightBool = reserveFlight(id,customer,flightNum);
				}
			}
		}

		/*
                if (room==true)
                {
                        roomBool = reserveRoom(id,customer,location);
                }

                // Reserve all the flights from the vector
                for (int i=0; i <flightNumbers.size(); i++)
                {
                        int flightNum = Integer.parseInt((String)flightNumbers.get(i));
                        flightBool = reserveFlight(id,customer,flightNum);
                }*/

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
