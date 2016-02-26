package utils;

import java.util.ArrayList;
import java.util.List;

import services.ClockService;
import services.TimeStamp;
import services.TimestampComparisonException;
import services.VectorTimeStamp;

public class Group {

	List<Node> nodes;
	ClockService groupClockService;
	String name;
	int prevSeqNum;
	
	
	public Group(List<Node> nodes, String groupName, String myName) throws ArrayIndexOutOfBoundsException {
		this.nodes = nodes;
		this.name = groupName;
		this.prevSeqNum = 0;
		int myIndex = findIndex(myName);
		this.groupClockService = ClockService.clockServiceFactory("vector", nodes.size(), myIndex);
		System.out.println(this.toString());
	}

	private int findIndex(String myName) throws ArrayIndexOutOfBoundsException {
		Node node;
		for (int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			if (node.name.equals(myName)) {
				return i;
			}
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	public synchronized ClockService getClockService() {
		return this.groupClockService;
	}

	public synchronized List<Node> getNodes() {
		List<Node> nodesList;
		nodesList = new ArrayList<Node>(this.nodes);
		return nodesList;
	}

	public String toString() {
		StringBuffer str = new StringBuffer("Name: ");

		str.append(String.format("%s\n", this.name));
		str.append(String.format("ClockService: %s\n", this.groupClockService.getTimeStamp().toString()));

		str.append("Nodes:\n");
		
		for (int i = 0; i<nodes.size(); i++){
			Node node = nodes.get(i);
			str.append("i="+Integer.toString(i)+" ");
			str.append(node.toString());
			str.append("\n");

		}
		
		return str.toString();
	}

	public int getProcessGroupIndex(String src) {
		Node node;
		for (int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			if (node.name.equals(src)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * 
	 * @param nodeName
	 * @return
	 */
	public synchronized TimeStamp getNewSendingTimeStamp(String nodeName) {
		if (nodeName == null || nodeName.isEmpty()) {
			return null;
		}
		int index = this.findIndex(nodeName);
		
		TimeStamp currentTimeStamp = groupClockService.getTimeStamp();
		int[] current_TS = (int[]) currentTimeStamp.get_value();
		this.prevSeqNum++;
		current_TS[index] = this.prevSeqNum;
		currentTimeStamp.set_value(current_TS);
		//groupClockService.setTimeStamp(currentTimeStamp);

		return currentTimeStamp;

	}

	

	/**
	 * Increments the current timestamp's index for this nodeName </br>
	 * and returns the new timestamp
	 * 
	 * @param nodeName
	 * @return newTimeStamp.clone()
	 */
	public synchronized TimeStamp getNewDeliveredTimeStamp(String nodeName) {
		if (nodeName == null || nodeName.isEmpty()) {
			return null;
		}
		// get index of this user
		int index = this.findIndex(nodeName);

		VectorTimeStamp currentTimeStamp = (VectorTimeStamp) groupClockService.getTimeStamp();
		int[] current_TS = (int[]) currentTimeStamp.get_value();
		current_TS[index]++;
		currentTimeStamp.set_value(current_TS);
		groupClockService.setTimeStamp(currentTimeStamp);

		return currentTimeStamp.clone();
	}

	/**
	 * @return t2[nodeName] - t1[nodeName] <br/>
	 */
	public int compareDelta(String nodeName, TimeStamp t1, TimeStamp t2) throws TimestampComparisonException {
		if (nodeName == null || nodeName.isEmpty() || t1 == null || t2 == null) {
			throw new TimestampComparisonException("Bad inputs");
		}

		int index = this.findIndex(nodeName);
		int[] time1 = (int[]) t1.get_value();
		int[] time2 = (int[]) t2.get_value();
		if (time1.length != time2.length) {
			throw new TimestampComparisonException("unequal vector lengths");
		}
		return time2[index] - time1[index];
	}

	/**
	 * Increments the current timestamp's index for this nodeName </br>
	 * and returns the new timestamp
	 * 
	 * @param nodeName
	 * @return newTimeStamp.clone()
	 */
	public synchronized TimeStamp generateTimeStamp(String nodeName, int delta) {
		if (nodeName == null || nodeName.isEmpty()) {
			return null;
		}
		// get index of this user
		int index = this.findIndex(nodeName);

		VectorTimeStamp newTimeStamp = (VectorTimeStamp) groupClockService.getTimeStamp();
		int[] current_TS = (int[]) newTimeStamp.get_value();
		current_TS[index] += delta;
		newTimeStamp.set_value(current_TS);
		return newTimeStamp;
	}

	/* Getters and Setters */
	public String getName() {
		return this.name;
	}

}
