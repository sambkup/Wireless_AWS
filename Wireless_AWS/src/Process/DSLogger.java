package process;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import communication.MessagePasser;
import communication.TimeStampedMessage;
import services.ClockService;
import services.DSEvent;
import services.TimeStamp;
import services.TimestampComparisonException;
import utils.Configuration;

public class DSLogger {

	private static String name;
	private static boolean debug = true;

	private static LinkedList<DSEvent> eventsLinkedList;
	private static ClockService clockService;
	private static Configuration config;

	private static MessagePasser msg_passer;

	public static void main(String[] args) {

		dbg_println("Starting Logger Service");

		// --------------------------------
		// initialize
		// if not command line input - prompt for inputs
		String config_file_address = null;
		if (args.length == 0) {
			config_file_address = initial_prompt(0);
		} else if (args.length > 1) {
			System.out.println("Usage: /path/to/file");
			System.exit(0);
		} else {
			// TODO: test command line arguments functionality for
			// errors/malformatting
			config_file_address = args[0];
		}

		// if config file not specified, use a default
		if (config_file_address == null || config_file_address.isEmpty()) {
			config_file_address = "resources/config.txt";
			// config_file_address =
			// "http://www.andrew.cmu.edu/user/skupfer/config.txt";
		}
		dbg_println("using config_file at: " + config_file_address);

		name = "logger";
		eventsLinkedList = new LinkedList<DSEvent>();

		// TODO: obtain these from a GUI
		config = new Configuration(name, config_file_address, 10, TimeUnit.SECONDS);

		clockService = ClockService.clockServiceFactory((String) config.getParameter("clockType"),
				config.getNodes().size(), config.getNodeIndex(name));
		msg_passer = new MessagePasser(config, clockService);

		// start the prompt thread to print history when requested
		prompt();

		// --------------------------------
		// Read logged messages and sort them
	}

	private static void dbg_println(String msg) {
		if (debug) {
			System.out.println(msg);
		}
	}

	private static void receiveUntilEmpty() {
		while (true) {
			TimeStampedMessage rcved = (TimeStampedMessage) msg_passer.receive();

			if (rcved == null) {
				break;
			}

			TimeStampedMessage event_message = (TimeStampedMessage) rcved.getData();
			TimeStamp event_timestamp = rcved.getTimeStamp();
			String event_kind = rcved.getKind();

			// turn this into an event object
			DSEvent newEvent = new DSEvent(event_timestamp, event_kind, event_message);

			boolean success = placeEvent(newEvent);
			if (!success) {
				dbg_println("Failed to add this message in order");
				continue;
			}
		}
	}

	private static void printEvents() {
		System.out.println("--------------------------------");
		synchronized (eventsLinkedList) {
			if (eventsLinkedList.size() < 1) {
				System.out.println("No Events to Log.");
			} else {
				DSEvent prev = eventsLinkedList.get(0);
				DSEvent cur = null;
				TimeStamp t1 = prev.getEventTimeStamp();
				TimeStamp t2, earlier = null;
				for (int i = 1; i < eventsLinkedList.size(); i++) {
					cur = eventsLinkedList.get(i);
					t2 = cur.getEventTimeStamp();
					try {
						earlier = clockService.compare(t1, t2);
						if (earlier == t1) {
							System.out.println(prev.toString());
						} else {
							System.out.printf("CONCURRENT WITH NEXT: %s\n", prev.toString());
						}
					} catch (TimestampComparisonException e) {
						// Can't compare
						System.out.printf("CAN'T COMPARE: %s\n", prev.toString());
					}

					prev = cur;
					t1 = t2;
				}
				cur = eventsLinkedList.getLast();
				if (cur != null) {
					System.out.println(cur.toString());
				}
			}
		}
		System.out.println("--------------------------------");
	}

	private static boolean placeEvent(DSEvent newEvent) {
		boolean result = placeEventSimple(newEvent);
		return result; // return false if a failure
	}

	private static boolean placeEventSimple(DSEvent newEvent) {

		// TODO: this may not be necessary (if the list is not yet populated)
		synchronized (eventsLinkedList) {
			int size = eventsLinkedList.size();
			if (size == 0) {
				eventsLinkedList.add(newEvent);
				return true;
			}

			TimeStamp newTimeStamp = newEvent.getEventTimeStamp();
			TimeStamp oldTimeStamp;

			// look through every timestamp in the list.
			// if this timestamp is before or concurrent, then add it at this
			// index
			// if not, keep going
			for (int i = 0; i < eventsLinkedList.size(); i++) {
				DSEvent event = eventsLinkedList.get(i);
				// compare timestamps
				oldTimeStamp = event.getEventTimeStamp();
				TimeStamp earlier = null;
				try {
					// returns earlier timestamp;
					earlier = clockService.compare(oldTimeStamp, newTimeStamp);
				} catch (TimestampComparisonException e) {
					continue; // i.e. could not determine relationship
				}
				if (earlier == null || earlier == oldTimeStamp) {
					continue;
				}
				int index = eventsLinkedList.indexOf(event);
				eventsLinkedList.add(index, newEvent);
				return true;
			}
			eventsLinkedList.add(newEvent);
			return true; // return false if a failure
		}
	}

	@SuppressWarnings("unused")
	private static boolean placeEventEfficient(DSEvent newEvent) {
		// TODO: us an efficient placing algorithm
		return false;
	}

	private static void prompt() {
		Thread receiver_thread = new Thread() {
			public void run() {
				while (true) {
					Object[] options = { "Yes" };
					int n = JOptionPane.showOptionDialog(null, "Print History? ", "Logger", JOptionPane.YES_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
					if (n == -1) {
						System.exit(0);
					}
					receiveUntilEmpty();
					printEvents();
				}
			}
		};

		receiver_thread.start();
	}

	private static String initial_prompt(int code) {

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(10, 10, 10, 10);

		JTextField file_field = new JTextField(20);

		JPanel myPanel = new JPanel();

		myPanel.setLayout(new GridBagLayout());

		constraints.gridx = 0;
		constraints.gridy = 0;
		myPanel.add(new JLabel("Config file address:"), constraints);

		constraints.gridx = 1;
		constraints.gridy = 0;
		myPanel.add(file_field, constraints);

		constraints.gridx = 0;
		constraints.gridy = 2;
		myPanel.add(new JLabel("~/path/to/file or http://url"), constraints);

		myPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Process Settings"));

		if (code == 1) {
			constraints.gridx = 1;
			constraints.gridy = 4;
			JLabel n = new JLabel("Please enter a name!");
			n.setForeground(Color.red);
			myPanel.add(n, constraints);
		}

		int result = JOptionPane.showConfirmDialog(null, myPanel, "Welcome!", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			return file_field.getText();
		} else if (result == -1 || result == JOptionPane.CANCEL_OPTION) {
			System.exit(0);
			return null;
		} else {
			return null;
		}
	}
}