// -------------------------------// adapted from Kevin T. Manley// CSE 593// -------------------------------package ResImpl;import java.io.*;// Resource manager data itempublic abstract class RMItem implements Serializable, Cloneable{    RMItem() {			super();    }        public RMItem clone() {    	try {    	return (RMItem)super.clone();    	}    	catch (CloneNotSupportedException er) {    		er.printStackTrace();    	}    	return null;    }}