package Process;

import communication.Message;
import communication.MessagePasser;
import utils.Configuration;

public class Client {

	private static String config_file_address;
	private static Configuration config;
	private static MessagePasser messagePasser;

	public static void main(String[] args) {
		// --------------------------------
		// initialize - get necessary parameters from input

		//config_file_address = "resources/config.txt";
		config_file_address = "http://52.36.135.251/config.txt";
		System.out.println("Config_file: " + config_file_address);

		// --------------------------------
		// instantiate the required objects

		config = new Configuration(config_file_address);
		config.print();
		
		messagePasser = new MessagePasser(config, false);

		// --------------------------------
		// Execute the receiver/sender scripts
		
		
		double start_lat = 40.442283630;
		double start_long = -79.947059641;
		double del_lat = .00010;
		double del_long = .00005;


		Message msg = new Message("sammy",start_lat,start_long,del_lat,del_long);
		messagePasser.send(msg);
		start_lat+=del_lat;
		start_long+=del_long;
		msg = new Message("sammy",start_lat,start_long,del_lat,del_long);
		messagePasser.send(msg);
		start_lat+=del_lat;
		start_long+=del_long;
		msg = new Message("sammy",start_lat,start_long,del_lat,del_long);
		messagePasser.send(msg);
		start_lat+=del_lat;
		start_long+=del_long;
		msg = new Message("sammy",start_lat,start_long,del_lat,del_long);
		messagePasser.send(msg);
		start_lat+=del_lat;
		start_long+=del_long;
		msg = new Message("sammy",start_lat,start_long,del_lat,del_long);
		messagePasser.send(msg);
		start_lat+=del_lat;
		start_long+=del_long;
		msg = new Message("sammy",start_lat,start_long,del_lat,del_long);
		messagePasser.send(msg);
		start_lat+=del_lat;
		start_long+=del_long;
		msg = new Message("sammy",start_lat,start_long,del_lat,del_long);
		messagePasser.send(msg);
		System.out.println("messages sent");
		System.exit(0);

	}

}
