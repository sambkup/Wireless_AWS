package communication;

import java.util.Arrays;

import services.TimeStamp;

public class MulticastMessage extends TimeStampedMessage {
	private static final long serialVersionUID = -1687020658302506970L;
	private String groupID = null;
	private int[] ackVector = null;

	public MulticastMessage(String groupID, int groupSize, String kind, Object data, TimeStamp newTimeStamp) {
		super(null, kind, data, newTimeStamp);
		this.groupID = groupID;
		this.ackVector = new int[groupSize];
		this.kind = kind;
		this.data = data;
		this.timeStamp = newTimeStamp;
	}

	public int[] getAck() {
		return this.ackVector;
	}

	public void addAck(int processIndex) {
		if (processIndex >= 0 && processIndex < ackVector.length) {
			this.ackVector[processIndex] = 1;
		} else {
			System.out.println("ArrayIndex Out of bounds: " + Integer.toString(processIndex));
		}
	}

	public boolean isReady() {
		for (int i : ackVector) {
			if (i != 1) {
				return false;
			}
		}
		return true;
	}

	public synchronized String getGroupID() {
		return groupID;
	}

	public synchronized void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public MulticastMessage clone() {
		MulticastMessage clone = new MulticastMessage(groupID, ackVector.length, kind, data, timeStamp.clone());
		clone.src = src;
		clone.dest = dest;
		clone.seqNum = seqNum;
		clone.ackVector = ackVector.clone();
		return clone;
	}
		
	public String toString() {
		return String.format("Message From: %s, To: %s, Kind: %s, SeqNum: %d, TimeStamp: %s, AckVector: %s, Data: %s, ", this.src,
				this.dest, this.kind, this.seqNum, this.timeStamp.toString(),Arrays.toString(this.ackVector), this.data == null ? "null" : this.data.toString());
	}

	

}
