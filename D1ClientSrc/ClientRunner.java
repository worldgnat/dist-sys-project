import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;


public class ClientRunner {
	public void main(String[] args) {
		LinkedBlockingQueue<Long> responseTimes = new LinkedBlockingQueue<Long>();
		
		ArrayList<client> clients = new ArrayList<client>();
		ArrayList<String> commands;
		
		try {
			for (String file : args) {
				commands = new ArrayList<String>();
				BufferedReader in = new BufferedReader(new FileReader(file));
				String line;
				while ((line = in.readLine()) != null) {
					commands.add(line);
				}
				//Create a new auto-client
				client temp = new client(responseTimes, commands);
				//Add it to the list of running clients
				clients.add(temp);
				//Spawn the thread that will run this client.
				new Thread(temp).start();
				in.close();
			}
			//Add a shutdown hook to make sure we killall the clients and print response time on ctrl+c
			Runtime.getRuntime().addShutdownHook(new Thread(new ClientKiller(responseTimes, clients)));
		}
		catch (Exception er) {
			er.printStackTrace();
		}
	}
}

class ClientKiller implements Runnable {
	ArrayList<client> clients;
	LinkedBlockingQueue<Long> responseTimes;
	public ClientKiller(LinkedBlockingQueue<Long> responseTimes, ArrayList<client> clients) {
		this.clients = clients;
		this.responseTimes = responseTimes;
	}
	
	public void run() {
		//Kill all running auto-clients
		for (client client : clients) {
			client.stop();
		}
		
		//Calculate the average response time for this run and print it
		long avgTime = 0;
		int numSamples = responseTimes.size();
		for (long time : responseTimes) {
			avgTime += time;
		}
		avgTime = avgTime / numSamples;
		System.out.println("Average Response Time: " + avgTime);
	}
}