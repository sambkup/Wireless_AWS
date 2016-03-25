package Process;

import communication.Message;
import communication.MessagePasser;

public class Client {

	private static String config_file_address;
	private static MessagePasser messagePasser;

	static String serverIP;
	static int serverPort;
	
	public static void main(String[] args) {
		// --------------------------------
		// initialize - get necessary parameters from input

		serverIP = "127.0.0.1";
		serverPort = 4001;
		System.out.println("Server address: " + config_file_address+":"+serverPort);

		// --------------------------------
		// instantiate the required objects
		
		messagePasser = new MessagePasser(serverIP,serverPort, true);

		// --------------------------------
		// Execute the receiver/sender scripts
		
		//      center: {lat: 40.442243, lng: -79.94701758}

		double start_lat = 	40.442243000;
		double start_long =-79.947010580;
		double del_lat = 	  .000007;
		double[] del_lats = {.000007, .000017, -.000007, -.000027,.000007};
		double del_long = 	  .00002;


		Message msg = new Message("sammy",start_lat,start_long,del_lat,del_long);
		messagePasser.send(msg);
		for (int k=0; k<5;k++){
			start_lat+=(del_lats[k]);
			start_long+=(del_long);
			msg = new Message("sammy",start_lat,start_long,del_lats[k],del_long);
			messagePasser.send(msg);
		}
		System.out.println("done");
		System.exit(0);

	}

}
