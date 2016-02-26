package process;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

import communication.MessagePasser;
import communication.TimeStampedMessage;
import services.ClockService;
import utils.Configuration;

public class Messenger {

	protected static final String receive_block = new String();
	private static String name;
	private static String config_file_address;
	private static boolean debug = true;
	private static Configuration config;
	private static ClockService globalClockService;
	private static MessagePasser messagePasser;

	private static void dbg_println(String msg) {
		if (debug) {
			System.out.println(msg);
		}
	}

	public static void main(String[] args) {
		// --------------------------------
		// initialize - get necessary parameters from input

		// if not command line input - prompt for inputs
		if (args.length == 0) {
			String[] results = initial_prompt(0);
			String sender = results[0];
			while (sender.isEmpty()) {
				results = initial_prompt(1);
				sender = results[0];
			}
			name = sender;
			config_file_address = results[1];
		} else if (args.length > 2) {
			System.out.println("Usage: name /path/to/file");
			System.exit(0);
		} else {
			name = args[0];
			config_file_address = args[1];
		}

		dbg_println("I am " + name);

		// if config file not specified, use a default
		if (config_file_address.isEmpty()) {
			config_file_address = "resources/config.txt";
			// config_file_address =
			// "http://www.andrew.cmu.edu/user/skupfer/config.txt";
		}
		dbg_println("using config_file at: " + config_file_address);

		// --------------------------------
		// instantiate the required objects

		config = new Configuration(name, config_file_address, 10, TimeUnit.SECONDS);
		globalClockService = ClockService.clockServiceFactory((String) config.getParameter("clockType"),
				config.getNodes().size(), config.getNodeIndex(name));
		messagePasser = new MessagePasser(config, globalClockService);

		// --------------------------------
		// Execute the receiver/sender scripts

				
		
		run_receiver(messagePasser);
		while (true) {
			message_prompt();
		}
	}
	
	private static void run_receiver(MessagePasser msg_passer) {
		Thread receiver_thread = new Thread() {
			public void run() {
				while (true) {
					synchronized (receive_block) {
						try {
							receive_block.wait();
						} catch (InterruptedException e) {
							System.out.println("failed to wait");
							e.printStackTrace();
						}
						TimeStampedMessage rcved = (TimeStampedMessage) msg_passer.receive();
						if (rcved != null) {
							rcved.setTimeStamp(globalClockService.getNewTimeStamp(rcved.getTimeStamp()));
							rcved.print();
						}
					}
				}
			}
		};

		receiver_thread.start();
	}

	private static void message_prompt() {
		Object[] options = { "Send", "Multicast", "Receive", "Critical Section" };

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(10, 10, 10, 10);

		JTextField xField = new JTextField(15);
		JTextField yField = new JTextField(15);
		JTextField zField = new JTextField(15);
		JTextField groupField = new JTextField(15);

		JPanel myPanel = new JPanel();

		myPanel.setLayout(new GridBagLayout());
		
		String[] rowMessages = {"Fill in the Message:", "Message destination:", "Message Payload:", "Message Kind:", "Group ID:"};
		JComponent[] components = {xField, yField, zField, groupField};
		int rows = rowMessages.length;
		for (int row=0; row<rows; row++) {
			constraints.gridx = 0;
			constraints.gridy = row;
			myPanel.add(new JLabel(rowMessages[row]), constraints);
			
			if (row == 0) continue;
			
			constraints.gridx = 1;
			constraints.gridy = row;
			myPanel.add(components[row-1], constraints);
		}

		myPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Message"));

		int result = JOptionPane.showOptionDialog(null, myPanel, "Process: " + name, JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		String dest = xField.getText();
		String payload = yField.getText();
		String kind = zField.getText();
		String groupID = groupField.getText();
		
		if (result == -1) {
			System.exit(0);
		} else if (options[result].equals("Send")) {
			TimeStampedMessage message = new TimeStampedMessage(dest, kind, payload,
					globalClockService.getNewTimeStamp(null));
			messagePasser.send(message);

		} else if (options[result].equals("Multicast")) {
			
			TimeStampedMessage message = new TimeStampedMessage(dest, kind, payload,
						globalClockService.getNewTimeStamp(null));
				messagePasser.multicast(groupID, message);
		} else if (options[result].equals("Receive")) {
			synchronized (receive_block) {
				receive_block.notify();
			}
		} else if (options[result].equals("Critical Section")) {
			messagePasser.runCS();
		}

	}

	private static String[] initial_prompt(int code) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(10, 10, 10, 10);

		JTextField name_field = new JTextField(20);
		JTextField file_field = new JTextField(20);

		JPanel myPanel = new JPanel();

		myPanel.setLayout(new GridBagLayout());

		constraints.gridx = 0;
		constraints.gridy = 0;
		myPanel.add(new JLabel("Process name:"), constraints);

		constraints.gridx = 1;
		constraints.gridy = 0;
		myPanel.add(name_field, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		myPanel.add(new JLabel("Config file address:"), constraints);

		constraints.gridx = 1;
		constraints.gridy = 1;
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
			String name = name_field.getText();
			String file_address = file_field.getText();
			String[] results = { name, file_address };
			return results;
		} else if (result == -1 || result == JOptionPane.CANCEL_OPTION) {
			System.exit(0);
			return null;
		} else {
			return null;
		}
	}	
}




