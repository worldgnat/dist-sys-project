package ResImpl;
import exceptions.InvalidTransactionException;
import exceptions.TransactionAbortedException;
import groupComm.AbortMessage;
import groupComm.CommitMessage;
import groupComm.HashtableUpdate;
import groupComm.ImThePrimary;
import groupComm.RMMessage;
import groupComm.StartMessage;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import ResInterface.MiddleResourceManageInt;

public class GroupManagement extends ReceiverAdapter {
	JChannel channel;
	MiddleResourceManageInt rm;
	
	boolean primary = false;
	boolean atMiddleware;
		
	public GroupManagement(MiddleResourceManageInt rm, String channelName) {
		this.rm = rm;
		atMiddleware = (rm.getClass().equals(Middleware.class));
		try {
			//Create the connection to the channel for this RM's group.
			channel=new JChannel();
			System.out.println("Test 1");
	        channel.setReceiver(this);
			System.out.println("Test 2");
	        channel.connect(channelName);
			System.out.println("Test 3");
	        channel.getState(null, 10000);
			System.out.println("Test 4");
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
        //List<Address> members = new_view.getMembers();
			System.out.println("Test 5");

        //The member with the lowest number is, arbitrarily, the primary copy
       /* Collections.sort(members);
        if (channel.getAddress().equals(members.get(0))) {
        	//Hey, look! We're the primary copy! Let's get this show on the road.
        	System.out.println("[GM - INFO] " + channel.getAddressAsString() + " is officially the king of the " + channel.getName() + "channel now.");
        	primary = true;
        }
        else primary = false;*/
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
    			channel.send(msg);
    		}
    		catch(Exception er ) {
    			System.err.println("[GM - ERROR] Error sending message to the group.");
    			er.printStackTrace();
    		}
    	}
    }
    
    public void receive(Message msg) {
        Object obj = msg.getObject();
        /*
         * Is the following an abuse of Java reflection and of Object Orientation in general? Probably. Do I care? No. No I don't.
         */
        try {
	        if (obj.getClass().equals(HashtableUpdate.class)) { //This is an update to our RM's hashtable
	        	HashtableUpdate update = (HashtableUpdate)obj;
	        	if (update.getValue() == null) { //This is a removal
	        		rm.removeData(update.getTid(), update.getKey());
	        	}
	        	else rm.writeData(update.getTid(), update.getKey(), update.getValue());
	        }
	        else if (obj.getClass().equals(StartMessage.class)) {
	        	rm.start(((StartMessage)obj).getTid());
	        }
	        else if (obj.getClass().equals(CommitMessage.class)) {
	        	rm.commit(((CommitMessage)obj).getTid());
	        }
	        else if (obj.getClass().equals(AbortMessage.class)) {
	        	rm.abort(((AbortMessage)obj).getTid());
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
    
    /*
     * 
     */
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
