package communication;

import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	String dest;
	String kind;
	Object data;
	String src;

	int seqNum;

	public Message(String dest, String kind, Object data) {
		this.dest = dest;
		this.kind = kind;
		this.data = data;
		this.seqNum = -1;
	}

	public String getSource() {
		return src;
	}

	public String getDest() {
		return dest;
	}

	public void set_source(String source) {
		this.src = source;
	}

	public void set_seqNum(int sequenceNumber) {
		this.seqNum = sequenceNumber;
		// set the sequence number for this message
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public void print() {
		System.out.println("------------------------------");
		System.out.println("Message From: " + this.src);
		System.out.println("Message To: " + this.dest);
		System.out.println("Message Kind: " + this.kind);
		System.out.println("Message Data: " + this.data.toString());
		System.out.println("Message SeqNum: " + this.seqNum);
		System.out.println("------------------------------");
	}

	public Message clone() {
		Message clone = new Message(dest, kind, data);
		clone.src = src;
		clone.seqNum = seqNum;
		return clone;
	}

	public String toString() {
		return String.format("Message From: %s, To: %s, Kind: %s, Data: %s, SeqNum: %d", this.src, this.dest, this.kind,
				this.data.toString(), this.seqNum);
	}
}
