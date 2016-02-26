package communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import communication.Connection.ConnectionStatus;
import mutex.CriticalManager;
import services.ClockService;
import services.TimeStamp;
import services.TimestampComparisonException;
import utils.Configuration;
import utils.Group;
import utils.Node;
import utils.Rule;
import utils.Rule.RuleActions;

public class MessagePasser {

	ClockService clockService;
	Configuration configuration;
	Node localNode;
	CriticalManager criticalManager;

	List<Message> delay_receive_queue;
	List<MulticastMessage> holdbackQueue;
	List<Message> receive_queue;
	List<Message> send_queue;

	HashMap<String, Connection> connection_list;

	public MessagePasser(Configuration config, ClockService newClockService) {
		this.clockService = newClockService;
		this.connection_list = new HashMap<String, Connection>();
		this.delay_receive_queue = new ArrayList<Message>();
		this.holdbackQueue = new ArrayList<MulticastMessage>();
		this.receive_queue = new ArrayList<Message>();
		this.send_queue = new ArrayList<Message>();
		this.criticalManager = new CriticalManager(config, this);


		this.configuration = config;
		this.localNode = config.getNode(config.getProcessName());

		if (this.localNode.ip == null || this.localNode.port == 0) {
			System.out.println("Unknown IP or Port!");
			return;
		}

		// run server
		Thread server = new Thread() {
			public void run() {
				listen_server();
			}
		};
		server.start();
	}

	public Connection open_connection(Node node) {

		if (node == null || node.ip == null || node.port == 0 || node.name == null) {
			return null;
		}
		Socket s = null;

		try {
			s = new Socket(node.ip, node.port);
			s.setKeepAlive(true);

			Connection c = new Connection(s, this);
			// c.print();
			synchronized (connection_list) {
				connection_list.put(node.name, c);
			}
			Message marco = new Message(node.name, "marco", "marco");
			marco.src = this.localNode.name;
			c.send_marco(marco);

			System.out.println("Sent Marco to node: " + node.toString());
			return c;
		} catch (UnknownHostException e) {
			System.out.println("Client Socket error:" + e.getMessage());
		} catch (EOFException e) {
			System.out.println("Client EOF error:" + e.getMessage());
		} catch (IOException e) {
			System.out.println("Client readline error:" + e.getMessage());
		}

		if (s != null)
			try {
				s.close();
			} catch (IOException e) {
				System.out.println("close error:" + e.getMessage());
			}
		return null;
	}

	Connection get_connection(String name) {
		synchronized (connection_list) {
			// lookup open connection in hashmap
			if (connection_list.containsKey(name)) {
				// if there is one, return it
				return connection_list.get(name);
			}
		}
		// else, open a new TCP socket and turn it into a connection
		return open_connection(this.configuration.getNode(name));
	}

	public synchronized void send(Message message) {
		Connection connection_to_use = get_connection(message.dest);
		if (connection_to_use == null) {
			System.out.println("Failed to find or open connection");
			return;
		}

		// send message via TCP:
		synchronized (connection_to_use) {
			connection_to_use.seqNum++;
			message.set_seqNum(connection_to_use.seqNum);
		}
		message.set_source(this.localNode.name);

		// check against rules
		RuleActions action = this.checkRules(message, this.configuration.getSendRules());
		if (action != RuleActions.none) {
			System.out.println("Send Rule: " + Rule.actionToString(action));
		}

		switch (action) {
		case delay:
			this.send_queue.add(message);
			return;
		case drop:
		case dropAfter:
			return;
		default:
			// Continue to send...
		}

		send_tcp(message, connection_to_use);

		// check if anything in delay queue needs to be sent and send it
		for (Message msg : new ArrayList<Message>(this.send_queue)) {
			connection_to_use = get_connection(msg.dest);
			if (connection_to_use == null) {
				System.out.println("Failed to find an open connection for delayed messages.");
				continue;
			}
			send_tcp(msg, connection_to_use);
			this.send_queue.remove(msg);
		}

	}

	private void send_tcp(Message message, Connection connection_to_use) {
		if (connection_to_use.status == ConnectionStatus.ready) {
			connection_to_use.write_object(message);
		} else {
			connection_to_use.enqueue(message);
		}

	}

	public Message receive() {
		int size = 0;
		synchronized (receive_queue) {
			size = receive_queue.size();
		}
		if (size < 1)
			return null;

		Message retreive = null;
		synchronized (receive_queue) {
			retreive = receive_queue.remove(0);
		}

		return retreive;
	}

	public synchronized void receive_message(Message message, Connection c) {
		//	System.out.println("Got " + message.kind + " from " + message.src);
		if (message.kind.equals("polo")) {
			c.set_status(ConnectionStatus.ready);
			return;
		} else if (message.kind.equals("marco")) {
			add_connection(message.src, c);
			return;
		}  

		// check formatting
		RuleActions action = this.checkRules(message, this.configuration.getReceiveRules());
		if (action != RuleActions.none) {
			System.out.println("Receive Rule: " + Rule.actionToString(action));
		}

		switch (action) {
		case delay:
			if (!message.kind.equals("multicast") && !message.kind.equals("req remulticast")){
				this.delay_receive_queue.add(message);
				return;
			}
			break;
		case drop:
		case dropAfter:
			return;
		default:
			// Continue to receive...
		}

		if (message.kind.equals("multicast")) {			
			receiveMulticastMessage((MulticastMessage) message);
			return;
		} else if (message.kind.equals("req remulticast")) {
			MulticastMessage mmsg = (MulticastMessage) message;
			MulticastMessage foundmmsg = null;
			try {
				foundmmsg = findMulticastMessage(this.localNode.name, mmsg);
			} catch (TimestampComparisonException e) {
				System.out.println("Couldn't compare timestamps.");
				e.printStackTrace();
			}
			if (foundmmsg != null) {
				foundmmsg.dest = mmsg.src;
				send(foundmmsg);
			}
			return;
		}else if(message.kind.equals("requestCS")){
			this.criticalManager.requestCS(message);
			return;
		}else if(message.kind.equals("releaseCS")){
			this.criticalManager.releaseCS(message);
			return;
		}else if(message.kind.equals("permissionCS")){
			this.criticalManager.permissionCS(message);
			return;
		}

		synchronized (receive_queue) {
			receive_queue.add(message);
		}

		// put delayed messages into receive queue
		for (Message msg : new ArrayList<Message>(this.delay_receive_queue)) {
			synchronized (receive_queue) {
				receive_queue.add(msg);
			}
		}
		delay_receive_queue.clear();
	}

	private synchronized void receiveMulticastMessage(MulticastMessage msg) {
		if (msg == null) {
			return;
		}
		Group group = configuration.getGroup(msg.getGroupID());
		Message message = (Message) msg.data;
		//message.seqNum = msg.seqNum;
		int delta = 0;
		try {
			delta = group.compareDelta(message.src, group.getClockService().getTimeStamp(), msg.timeStamp);
		} catch (TimestampComparisonException e1) {
			System.out.println("Couldn't compare timestamps.");
			e1.printStackTrace();
		}

		// Drop message if it has already been received by the application
		if (delta <= 0) {
			return;
		} else if (delta > 1) {
			// we might be missing some messages. Request the sender to send you
			// the missing messages.
			MulticastMessage foundmmsg = null;
			for (int i = 1; i < delta; i++) {
				foundmmsg = findMulticastMessage(message.src, group, i);
				if (foundmmsg != null) {
					continue;
				} else {
					// send a rebroadcast request to message.src
					requestRemulticast(group, message.src, group.generateTimeStamp(message.src, i));
				}
			}
		}

		// if timestamp equal to group, check ack
		MulticastMessage trackedMsg = null;
		try {
			trackedMsg = findMulticastMessage(message.src, msg);
		} catch (TimestampComparisonException e) {
			System.out.println("Couldn't compare timestamps.");
			e.printStackTrace();
		}
		if (trackedMsg == null) {
			int processIndex = group.getProcessGroupIndex(msg.src);
			if (processIndex == -1 ) { System.out.println("bad index");}
			msg.addAck(processIndex);
			processIndex = group.getProcessGroupIndex(this.localNode.name);
			if (processIndex == -1 ) { System.out.println("bad index");}
			msg.addAck(processIndex);

			synchronized (this.holdbackQueue) {
				this.holdbackQueue.add(msg);
				if (!msg.src.equals(this.localNode.name)){
					remulticast(msg.clone());
				}
			}

		} else {
			int processIndex = group.getProcessGroupIndex(msg.src);
			trackedMsg.addAck(processIndex);
			if (trackedMsg.isReady() && delta == 1) {
				// deliver this message and the next ones that are ready
				// increment delta and loop through holdbackqueue, and
				// compare deltas
				deliverMulticastMessage(trackedMsg);
				MulticastMessage foundmmsg = null;
				do {
					foundmmsg = findMulticastMessage(message.src, group, delta);
					if (foundmmsg != null && foundmmsg.isReady()) {
						deliverMulticastMessage(foundmmsg);
					}
					else{
						break;
					}
				} while (foundmmsg != null);
			}
		}		

	}

	private void deliverMulticastMessage(MulticastMessage mmsg) {
		Message message = (Message) mmsg.data;
		Group group = configuration.getGroup(mmsg.getGroupID());
		group.getNewDeliveredTimeStamp(message.src);
		synchronized (holdbackQueue) {
			holdbackQueue.remove(mmsg);
		}
		receive_message(message, null);
	}

	private MulticastMessage findMulticastMessage(String nodeName, Group group, int delta) {
		synchronized (holdbackQueue){
			for (MulticastMessage mmsg : this.holdbackQueue) {
				Message msg = (Message) mmsg.getData();
				if (mmsg.getGroupID().equals(group.getName()) && msg.src.equals(nodeName)) {
					boolean deltaEqual = false;
					try {
						deltaEqual = delta == group.compareDelta(msg.src,group.getClockService().getTimeStamp(),
								mmsg.timeStamp);
					} catch (TimestampComparisonException e) {
						System.out.println("Couldn't compare timestamps");
						e.printStackTrace();
					}
					if (deltaEqual) {
						return mmsg;
					}
				}
			}
		}

		return null;
	}

	private MulticastMessage findMulticastMessage(String nodeName, MulticastMessage message)
			throws TimestampComparisonException {
		Group group = configuration.getGroup(message.getGroupID());
		synchronized(this.holdbackQueue){
			for (MulticastMessage msg : this.holdbackQueue) {
				if (msg.getGroupID().equals(message.getGroupID())
						&& group.compareDelta(nodeName, msg.timeStamp, message.timeStamp) == 0) {
					return msg;
				}
			}
			return null;
		}
	}

	private void listen_server() {
		System.out.println("Starting MessagePasser server with address = " + this.localNode.address);
		int counter = 0;
		ServerSocket listenSocket = null;
		try {
			listenSocket = new ServerSocket(this.localNode.port);

			while (true) {
				Socket clientSocket = listenSocket.accept();

				new Connection(clientSocket, this);
				System.out.println("Server received a new connection: # " + counter);
				counter++;
			}
		} catch (IOException e) {
			System.out.println("Listen socket:" + e.getMessage());
		} finally {
			try {
				if (listenSocket != null)
					listenSocket.close();
			} catch (IOException e) {
				System.out.println("Failed to close server.");
			}
		}

	}

	private RuleActions checkRules(Message message, List<Rule> rules) {

		String kind = message.kind;
		String src = message.src;
		String dest = message.dest;
		int seqNum = message.seqNum;

		for (Rule rule : rules) {
			if (rule.kind != null && !rule.kind.equals(kind)) {
				continue;
			}
			if (rule.src != null && !rule.src.equals(src)) {
				continue;
			}
			if (rule.dest != null && !rule.dest.equals(dest)) {
				continue;
			}

			if (rule.seqNum != -1)
			{

				if (rule.action == RuleActions.dropAfter && rule.seqNum >= seqNum) {
					continue;
				} else if (rule.action != RuleActions.dropAfter && rule.seqNum != seqNum) {
					continue;
				}
			}

			return rule.action;
		}

		return RuleActions.none;
	}

	private Connection compare_connections(Connection existing, Connection remote) {
		int remotePort = remote.clientSocket.getPort();
		String remoteAddressStr = remote.clientSocket.getInetAddress().toString();
		remoteAddressStr = remoteAddressStr.replace(".", "");
		remoteAddressStr = remoteAddressStr.replace("/", "");
		String remoteValueStr = String.format("%d%s", remotePort, remoteAddressStr);
		long remoteValue = Long.parseLong(remoteValueStr);

		int localPort = existing.clientSocket.getLocalPort();
		String localAddressStr = existing.clientSocket.getLocalAddress().toString();
		localAddressStr = localAddressStr.replace(".", "");
		localAddressStr = localAddressStr.replace("/", "");
		String localValueStr = String.format("%d%s", localPort, localAddressStr);
		long localValue = Long.parseLong(localValueStr);

		return localValue > remoteValue ? existing : remote;
	}

	public void add_connection(String name, Connection connection) {
		Connection existing = connection_list.get(name);
		if (existing != null && existing.status != ConnectionStatus.ready && !name.equals(this.localNode.name)) {
			// compare
			Connection winner = compare_connections(existing, connection);
			if (winner == existing) {
				connection.close();
				return;
			} else {
				if (!existing.message_queue.isEmpty()) {
					connection.message_queue.addAll(existing.message_queue);
				}
				existing.close();
			}
		}

		synchronized (connection_list) {
			connection_list.put(name, connection);
		}

		Message polo = new Message(name, "polo", "polo");
		polo.src = this.localNode.name;
		connection.send_polo(polo);

		connection.set_status(ConnectionStatus.ready);
	}

	public void remove_connection(Connection connection) {
		synchronized (connection_list) {
			connection_list.remove(connection.clientSocket.getRemoteSocketAddress().toString());
		}
	}

	public void multicast(String groupID, TimeStampedMessage message) {
		if (message == null) {
			return;
		}
		Group group = configuration.getGroup(groupID);

		if (group == null) {
			return;
		}

		TimeStamp newTimeStamp = group.getNewSendingTimeStamp(this.localNode.name);

		message.src = localNode.name;
		MulticastMessage multiCastMessage = new MulticastMessage(groupID, group.getNodes().size(), "multicast", message,
				newTimeStamp);
		Thread multicast_send_thread = new Thread(new multicastSendRunnable(group, multiCastMessage));
		multicast_send_thread.start();
	}


	public void runCS(){
		criticalManager.doSomething();
	}

	private void remulticast(MulticastMessage message) {
		Group group = configuration.getGroup(message.getGroupID());

		if (group == null) {
			return;
		}
		message.src = localNode.name;
		Thread multicast_send_thread = new Thread(new multicastSendRunnable(group, message));
		multicast_send_thread.start();
	}

	private void requestRemulticast(Group group, String nodeName, TimeStamp timestamp) {
		MulticastMessage message = new MulticastMessage(group.getName(), group.getNodes().size(), "req remulticast",
				null, timestamp);
		message.dest = nodeName;
		message.set_source(this.localNode.name);
		send(message);
	}

	class multicastSendRunnable implements Runnable {
		Group group;
		MulticastMessage multiCastMessage;

		multicastSendRunnable(Group group, MulticastMessage message) {
			this.group = group;
			this.multiCastMessage = message;
		}

		public void run() {
			if (group == null) {
				return;
			}

			for (Node node : group.getNodes()) {
				this.multiCastMessage.dest = node.name;
				send(this.multiCastMessage.clone());
			}
		}
	}

	public void printHBqueue(){
		System.out.println("----Printing the holdback Queue---");

		for (MulticastMessage msg : holdbackQueue){
			System.out.println(msg.toString());
		}
		System.out.println("----------------------------------");
	}

}

class Connection extends Thread {
	public enum ConnectionStatus {
		none, open, ready, closed
	}

	DataInputStream in;
	DataOutputStream out;
	ObjectOutputStream outObj;
	ObjectInputStream inObj;

	volatile int seqNum = -1;
	ConnectionStatus status = ConnectionStatus.closed;
	Socket clientSocket;
	MessagePasser msg_passer;
	List<Message> message_queue = new ArrayList<Message>();

	public Connection(Socket aClientSocket, MessagePasser msg_passer) {
		this.msg_passer = msg_passer;
		try {
			clientSocket = aClientSocket;
			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());
			outObj = new ObjectOutputStream(clientSocket.getOutputStream());
			inObj = new ObjectInputStream(clientSocket.getInputStream());

			this.start();
			status = ConnectionStatus.open;
		} catch (IOException e) {
			System.out.println("Connection:" + e.getMessage());
		}
	}

	public void run() {
		try {
			while (true) {
				Message message = (Message) inObj.readObject();
				msg_passer.receive_message(message, this);
			}

		} catch (EOFException e) {
			System.out.println("Server EOF:" + e.getMessage());
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
			System.out.println("Failed to convert received object!");
		} finally {
			try {
				if (clientSocket != null) {
					msg_passer.remove_connection(this);
					clientSocket.close();
				}
			} catch (IOException e) {
				System.out.println("Failed to close connection!");
			}
		}

	}

	public void write_object(Object object) {
		try {
			synchronized (outObj) {
				this.outObj.writeObject(object);
			}
		} catch (IOException e) {
			System.out.println("error sending message - client side:" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void enqueue(Message message) {
		synchronized (message_queue) {
			this.message_queue.add(message);
		}
	}

	public void set_status(ConnectionStatus status) {
		synchronized (this.status){
			this.status = status;
			synchronized (message_queue) {
				if (status == ConnectionStatus.ready && !this.message_queue.isEmpty()) {
					for (Message msg : message_queue) {
						write_object(msg);
					}
					message_queue.clear();
				}
			}
		}
	}

	public void send_marco(Message marco) {
		this.write_object(marco);
	}

	public void send_polo(Message polo) {
		this.write_object(polo);
	}

	public void close() {
		try {
			this.clientSocket.close();
		} catch (IOException e) {
			System.out.println("Failed to close connection: ");
			this.print();
			e.printStackTrace();
		}
	}

	public static String statusToString(ConnectionStatus status) {
		switch (status) {
		case closed:
			return "closed";
		case open:
			return "open";
		case ready:
			return "ready";
		default:
			return "none";
		}
	}

	public void print() {
		System.out.println("Connection status: " + statusToString(this.status));
		System.out.println("Remote Address: " + this.clientSocket.getRemoteSocketAddress());
		System.out.println("Local Address: " + this.clientSocket.getLocalSocketAddress());
	}
}
