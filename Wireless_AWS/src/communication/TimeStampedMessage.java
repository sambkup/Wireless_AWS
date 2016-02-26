package communication;

import services.TimeStamp;

public class TimeStampedMessage extends Message {
	private static final long serialVersionUID = -3378740060234927435L;
	TimeStamp timeStamp;

	public TimeStampedMessage(String dest, String kind, Object data, TimeStamp newTimeStamp) {
		super(dest, kind, data);
		//this.timeStamp = newTimeStamp.clone();
		this.timeStamp = newTimeStamp;
	}

	public TimeStamp getTimeStamp() {
		return timeStamp.clone();
	}

	public void setTimeStamp(TimeStamp timeStamp) {
		this.timeStamp = timeStamp.clone();
	}

	public void print() {
		System.out.println("------------------------------");
		System.out.println("Message From: " + this.src);
		System.out.println("To: " + this.dest);
		System.out.println("Kind: " + this.kind);
		System.out.println("Data: " + this.data.toString());
		System.out.println("SeqNum: " + this.seqNum);
		System.out.println("TimeStamp: " + this.timeStamp.toString());
		System.out.println("------------------------------");
	}

	public TimeStampedMessage clone() {
		TimeStampedMessage clone = new TimeStampedMessage(dest, kind, data, timeStamp.clone());
		clone.src = src;
		clone.seqNum = seqNum;
		return clone;
	}

	public String toString() {
		return String.format("Message From: %s, To: %s, Kind: %s, Data: %s, SeqNum: %d, TimeStamp: %s", this.src,
				this.dest, this.kind, this.data.toString(), this.seqNum, this.timeStamp.toString());
	}

}
