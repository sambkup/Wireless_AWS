package communication;

import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	
	double lattitude;
	double longitude;
	double vector_x;
	double vector_y;

	public Message(double lat, double lon, double vecx, double vecy) {
		this.lattitude = lat;
		this.longitude = lon;
		this.vector_x = vecx;
		this.vector_y = vecy;
	}

	public void print() {
		System.out.println("------------------------------");
		System.out.printf("Coordinates: %f,%f\n",this.lattitude, this.longitude);
		System.out.printf("Path: <%f,%f>\n", this.vector_x, this.vector_y);
		System.out.println("------------------------------");
	}

	public Message clone() {
		Message clone = new Message(this.lattitude, this.longitude, this.vector_x, this.vector_y);
		return clone;
	}
}
