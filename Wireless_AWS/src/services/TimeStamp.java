package services;

import java.io.Serializable;

import services.ClockService.ClockTypes;

public abstract class TimeStamp implements Serializable {
	private static final long serialVersionUID = -5493907164254527788L;

	public static TimeStamp timeStampFactory(ClockTypes type) {
		return timeStampFactory(type, 0);
	}

	public static TimeStamp timeStampFactory(ClockTypes type, int size) {
		switch (type) {
		case logical:
			return new LogicalTimeStamp();
		case vector:
			return new VectorTimeStamp(size);
		default:
			return null;

		}
	}

	// TODO: make sure htis is OK

	// method to get_value
	public abstract Object get_value();

	// method to update_value
	public abstract void set_value(Object newTime);

	public abstract TimeStamp clone();

	public abstract String toString();

}
