package communication;

import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	
	int lattitude;
	int longitude;
	int vector_x;
	int vector_y;

	int seqNum;

	public Message(int lat, int lon, int vecx, int vecy) {
		this.lattitude = lat;
		this.longitude = lon;
		this.vector_x = vecx;
		this.vector_y = vecy;
		this.seqNum = -1;
	}

	public void set_seqNum(int sequenceNumber) {
		this.seqNum = sequenceNumber;
		// set the sequence number for this message
	}

	public void print() {
		System.out.println("------------------------------");
		System.out.printf("Coordinates: %d,%d\n",this.lattitude, this.longitude);
		System.out.printf("Path: <%d,%d>\n", this.vector_x, this.vector_y);
		System.out.println("Message SeqNum: " + this.seqNum);
		System.out.println("------------------------------");
	}

	public Message clone() {
		Message clone = new Message(this.lattitude, this.longitude, this.vector_x, this.vector_y);
		clone.seqNum = this.seqNum;
		return clone;
	}
}
