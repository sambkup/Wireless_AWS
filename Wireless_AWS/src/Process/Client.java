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

		config_file_address = "resources/config.txt";
		// config_file_address = "http://www.andrew.cmu.edu/user/skupfer/config.txt";
		//System.out.println("Config_file: " + config_file_address);

		// --------------------------------
		// instantiate the required objects

		config = new Configuration(config_file_address);
		//config.print();
		
		messagePasser = new MessagePasser(config, false);

		// --------------------------------
		// Execute the receiver/sender scripts

		Message msg = new Message(54.0678,54.0678,10,10);
		messagePasser.send(msg);

		msg = new Message(55.0678,55.0678,15,15);
		messagePasser.send(msg);
		System.exit(0);

	}

}
