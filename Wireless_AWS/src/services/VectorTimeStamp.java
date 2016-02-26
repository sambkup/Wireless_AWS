package services;

import java.util.Arrays;

public class VectorTimeStamp extends TimeStamp {
	private static final long serialVersionUID = 21144324012540357L;
	int[] time;

	public VectorTimeStamp(int size) {
		// initialize the vector time stamp to all zeros
		if (size > 0) {
			time = new int[size];
		} else {
			System.out.println("Cannot create vector timestamp with length<1");
		}
	}

	@Override
	public int[] get_value() {
		return time.clone();
	}

	@Override
	public void set_value(Object value) {
		int[] newTime = (int[]) value;
		this.time = newTime.clone();
	}

	@Override
	public TimeStamp clone() {
		VectorTimeStamp clone = new VectorTimeStamp(time.length);
		clone.set_value(this.time);
		return clone;
	}

	@Override
	public String toString() {
		return Arrays.toString(this.time);
	}

}
