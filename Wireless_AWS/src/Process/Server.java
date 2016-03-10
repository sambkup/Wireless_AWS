package Process;

import communication.Message;
import communication.MessagePasser;
import utils.Configuration;

public class Server {
	static String config_file_address;
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
		
		messagePasser = new MessagePasser(config, true);

		// --------------------------------
		// Execute the receiver/sender scripts


		run_receiver(messagePasser);
		
		/*
		 * TODO: Re-factor and simplify everything:
		 * -Simplify config file so it only has the basics: port, IPs, etc.
		 * -Modify Message, so that it sends a name and GPS coordiantes
		 * 
		 */

	}

	private static void run_receiver(MessagePasser msg_passer) {
		Thread receiver_thread = new Thread() {
			public void run() {
				while (true) {
					synchronized (messagePasser.receive_block) {
						try {
							messagePasser.receive_block.wait();
						} catch (InterruptedException e) {
							System.out.println("failed to wait");
							e.printStackTrace();
						}
						Message rcved = msg_passer.receive();
						if (rcved != null) {
							rcved.print();
						}
					}
				}
			}
		};

		receiver_thread.start();
	}
}
