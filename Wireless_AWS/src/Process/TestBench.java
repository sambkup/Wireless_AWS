package process;

import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import communication.MessagePasser;
import communication.TimeStampedMessage;
import services.ClockService;
import utils.Configuration;

public class TestBench {

	//private static String name;
	private static String config_file_address;

	private static Configuration config_sammy;
	private static Configuration config_armin;
	private static ClockService globalClockService_sammy;
	private static ClockService globalClockService_armin;
	private static MessagePasser messagePasser_sammy;
	private static MessagePasser messagePasser_armin;

	public static void main(String[] args) {
		// --------------------------------
		// initialize - get necessary parameter inputs

		//name = "sammy";
		config_file_address = "resources/config.txt";
		// config_file_address =
		// "http://www.andrew.cmu.edu/user/skupfer/config.txt";

		// --------------------------------
		// construct the required objects

		config_sammy = new Configuration("sammy", config_file_address, 10, TimeUnit.SECONDS);
		config_armin = new Configuration("armin", config_file_address, 10, TimeUnit.SECONDS);
		
		globalClockService_sammy = ClockService.clockServiceFactory((String) config_sammy.getParameter("clockType"),
				config_sammy.getNodes().size(), config_sammy.getNodeIndex("sammy"));
		
		globalClockService_armin = ClockService.clockServiceFactory((String) config_armin.getParameter("clockType"),
				config_armin.getNodes().size(), config_armin.getNodeIndex("armin"));


		messagePasser_sammy = new MessagePasser(config_sammy, globalClockService_sammy);
		messagePasser_armin = new MessagePasser(config_armin, globalClockService_armin);

		// --------------------------------
		// Execute something here
		
//		TimeStampedMessage data_msg = new TimeStampedMessage("test", "msg1", "actual message",
//				globalClockService_sammy.getNewTimeStamp(null));
//		messagePasser_sammy.multicast("Group1", data_msg);
//
//		data_msg = new TimeStampedMessage("test", "msg2", "actual message", globalClockService.getNewTimeStamp(null));
//		messagePasser.multicast("Group1", data_msg);
		
		
		
		for (int i = 0; i<50; i++){
			String dest = "";
			String kind = "";
			String payload = "msg ";
			TimeStampedMessage message_sammy = new TimeStampedMessage(dest, kind, payload+Integer.toString(i),
					globalClockService_sammy.getNewTimeStamp(null));
			TimeStampedMessage message_armin = new TimeStampedMessage(dest, kind, payload+Integer.toString(i),
					globalClockService_armin.getNewTimeStamp(null));

			messagePasser_sammy.multicast("Group1", message_sammy);
			messagePasser_armin.multicast("Group1", message_armin);
			
		}
		
		
		prompt();


		
		
	}

	private static void prompt() {
		Thread receiver_thread = new Thread() {
			public void run() {
				while (true) {
					Object[] options = { "Quit" };
					int n = JOptionPane.showOptionDialog(null, null, "TestBench", JOptionPane.CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
					if (n == -1 || n == 0) {
						messagePasser_sammy.printHBqueue();
						messagePasser_armin.printHBqueue();

						System.exit(0);

					}
				}
			}
		};

		receiver_thread.start();
	}
}
