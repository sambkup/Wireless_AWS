package Process;

import communication.Message;
import communication.MessagePasser;
import utils.Configuration;

public class Server {
	static String config_file_address;
	private static boolean debug = true;
	private static Configuration config;
	private static MessagePasser messagePasser;

	public static void main(String[] args) {
		
		// --------------------------------
		// initialize - get necessary parameters from input

		config_file_address = "resources/config.txt";
		// config_file_address = "http://www.andrew.cmu.edu/user/skupfer/config.txt";
		dbg_println("using config_file at: " + config_file_address);

		// --------------------------------
		// instantiate the required objects

		config = new Configuration(config_file_address);
		dbg_println(config.getServerIP());
		dbg_println(Integer.toString(config.getServerPort()));
		
		messagePasser = new MessagePasser(config, true);

		// --------------------------------
		// Execute the receiver/sender scripts
		Message msg = new Message(10,10,10,10);

		messagePasser.send(msg);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Message msg2 = messagePasser.receive();
		msg2.print();
		
		
		/*
		 * TODO: Re-factor and simplify everything:
		 * -Remove rule checking
		 * -Simplify config file so it only has the basics: port, IPs, etc.
		 * -Modify Message, so that it sends a name and GPS coordiantes
		 * 
		 */

	}

	private static void dbg_println(String string) {
		if (debug ){
			System.out.println(string);
		}
		
	}

}
