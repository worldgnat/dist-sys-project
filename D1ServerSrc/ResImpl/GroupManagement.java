package ResImpl;
import exceptions.InvalidTransactionException;
import exceptions.TransactionAbortedException;
import groupComm.AbortMessage;
import groupComm.CommitMessage;
import groupComm.HashtableUpdate;
import groupComm.ImThePrimary;
import groupComm.RMMessage;
import groupComm.RequestPrimary;
import groupComm.StartMessage;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.util.Util;

import ResInterface.MiddleResourceManageInt;
import ResInterface.MiddlewareInt;
import ResInterface.ResourceManager;

public class GroupManagement extends ReceiverAdapter {
	JChannel channel;
	MiddleResourceManageInt rm;
	final List<String> state=new LinkedList<String>();
	TreeMap<String, JChannel> openChannels = new TreeMap<String, JChannel>();
	
	TreeMap<String, String> configs = new TreeMap<String, String>();
	
	MessageDispatcher disp;

	boolean primary = false;
	boolean atMiddleware;
	boolean isActive = false;
	
	public GroupManagement(MiddleResourceManageInt rm, String channelName) {
		this.rm = rm;
		atMiddleware = (rm.getClass().equals(Middleware.class));
		configs.put("flights29", "flights_udp.xml");
		configs.put("rooms29", "rooms_udp.xml");
		configs.put("cars29", "cars_udp.xml");
		configs.put("middleware29", "middleware_udp.xml");
		try {
			//Create the connection to the channel for this RM's group.
			System.out.println(channelName);
			channel=new JChannel(configs.get(channelName));
	        channel.setReceiver(this);
			disp = new MessageDispatcher(channel, this, this);
	        channel.connect(channelName);
	        channel.setDiscardOwnMessages(true); //I love JGroups. I really do.
	        channel.getState(null, 10000);
		}
		catch(Exception er) {
			er.printStackTrace();
			System.err.println("[GM - ERROR] Couldn't create the connection to the JChannel.");
		}
	}
	
	/*
	 * Catches changes in the view. This will happen in the case that we have added a group,
	 * or that the group has decided that a node has failed.
	 * 
	 * The Middleware will figure out that this is the primary node through a similar procedure.
	 * (non-Javadoc)
	 * @see org.jgroups.ReceiverAdapter#viewAccepted(org.jgroups.View)
	 */
    public void viewAccepted(View new_view) {
        System.out.println("[GM - INFO] New view: " + new_view);
        if (!isActive) isActive = true; //If this is the first new view we've gotten, then let everyone know we're up and running!
        List<Address> members = new ArrayList<Address>();
        members.addAll(new_view.getMembers());
        //The member with the lowest number is, arbitrarily, the primary copy
        Collections.sort(members);
        if (channel.getAddress().equals(members.get(0))) {
        	//Hey, look! We're the primary copy! Let's get this show on the road.
        	System.out.println("[GM - INFO] " + channel.getAddressAsString() + " is officially the king of the " + channel.getClusterName() + "channel now.");
        	primary = true;
        	if (!rm.getClass().equals(Middleware.class)) notifyPrimary();
        }
        else primary = false;
        if ( rm.m_itemHT != null )System.out.println("RM entry size: " + rm.m_itemHT.size());
    }
    
    /*
     * The RM will call this to send updates to the backup copies. 
     * If this is the primary copy, it will multicast the message to the rest of the group.
     * If not, it will do nothing.
     */
    public void sendUpdates(RMMessage update) {
    	if (primary) { 
    		try {
    			Message msg = new Message(null, null, update);
    			//channel.send(msg);
    			disp.castMessage(null, msg, new RequestOptions(ResponseMode.GET_ALL, 0));
    		}
    		catch(Exception er ) {
    			System.err.println("[GM - ERROR] Error sending message to the group.");
    			er.printStackTrace();
    		}
    	}
    }
    
    public void notifyPrimary() {
    	try {
    		JChannel midChannel = getChannel("middleware29");
        	midChannel.send(new Message(null, null, new ImThePrimary(java.net.InetAddress.getLocalHost().getCanonicalHostName(), rm.getPort(), channel.getClusterName())));
        	Thread.sleep(100); //This is a horrible solution... but it's past my bedtime.
        	midChannel.close();
        	openChannels.remove("middleware29");
    	}
    	catch (Exception er) {
    		System.err.println("[GM - ERROR] Failed to notify Middleware of primary status.");
    		er.printStackTrace();
    	}
    }
 
    public void receive(Message msg) {
        Object obj = msg.getObject();
        /*
         * Is the following an abuse of Java reflection and of Object Orientation in general? Probably. Do I care? No. No I don't.
         */
        try {
        	if (rm != null) {
		        if (obj.getClass().equals(HashtableUpdate.class)) { //This is an update to our RM's hashtable
		        	HashtableUpdate update = (HashtableUpdate)obj;
		        	if (update.getValue() == null) { //This is a removal
		        		rm.removeData(update.getTid(), update.getKey());
		        	}
		        	else rm.writeData(update.getTid(), update.getKey(), update.getValue(), null);
		        }
		        else if (obj.getClass().equals(StartMessage.class)) {
		        	rm.start(((StartMessage)obj).getTid(), null);
		        }
		        else if (obj.getClass().equals(CommitMessage.class)) {
		        	rm.commit(((CommitMessage)obj).getTid(), null);
		        }
		        else if (obj.getClass().equals(AbortMessage.class)) {
		        	rm.abort(((AbortMessage)obj).getTid(), null);
		        }
		        else if (obj.getClass().equals(ImThePrimary.class)) {
		        	ImThePrimary message = (ImThePrimary)obj;
		        	rm.setPrimary(message.getHostname(), message.getPort(), message.getChannel());
		        }
        	}
        	else {
        		System.err.println("[GM - ERROR] Resource manager has not been initialized.");
        	}
        }
        catch(RemoteException er) {
        	System.err.println("[GM - ERROR] Problem running message on RM.");
        	er.printStackTrace();
        }
        catch(TransactionAbortedException er ) {
        	System.err.println("[GM - ERROR] Transaction has been aborted.");
        }
        catch(InvalidTransactionException er ) {
        	System.err.println("[GM - ERROR] Invalid transaction received.");
        }
    }
    
    public void setState(byte[] new_state) {
    	try {
            rm =(ResourceManagerImpl)Util.objectFromByteBuffer(new_state);
            System.out.println("[GM - INFO] Received new state.");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public JChannel getChannel(String channelName) {
    	JChannel tempChannel;
    	if (!openChannels.containsKey(channelName)) {
    		try {
    			tempChannel = new JChannel(configs.get(channelName));
    			//tempChannel.setReceiver(this);
    			tempChannel.connect(channelName);
    			openChannels.put(channelName, tempChannel);
    		}
    		catch (Exception er ) {
    			System.err.println("[GM - ERROR] Failed to connect to channel " + channel);
    			tempChannel = null;
    		}
    	}
    	else tempChannel = openChannels.get(channelName);
    	return tempChannel;
    }
    
    public byte[] getState() {
    	try {
	    	synchronized(rm) {
	    		return Util.objectToByteBuffer(rm);
	    	}
    	}
    	catch(Exception er) {
    		er.printStackTrace();
    	}
    	return null;
    }
}

    