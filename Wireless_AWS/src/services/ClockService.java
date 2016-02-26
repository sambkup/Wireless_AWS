package services;

import java.io.Serializable;

public abstract class ClockService implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2592493082755289613L;

	public enum ClockTypes {
		none, logical, vector
	}

	public static ClockService clockServiceFactory(String type) {
		return clockServiceFactory(type, 0, 0);
	}

	public static ClockService clockServiceFactory(String type, int size, int myIndex) {
		switch (stringToClockType(type)) {
		case logical:
			return new LogicalClock();
		case vector:
			return new VectorClock(size, myIndex);
		default:
			return null;
		}
	}

	public static ClockTypes stringToClockType(String type) {
		if (type.equals("logical")) {
			return ClockTypes.logical;
		} else if (type.equals("vector")) {
			return ClockTypes.vector;
		}

		return ClockTypes.none;
	}

	// get current timestamp
	public abstract TimeStamp getTimeStamp();

	public abstract void setTimeStamp(TimeStamp timeStamp);

	// get a new timestamp - decided if increment or max -> return new timestamp
	// Object timeStamp is null, when we just want to increment
	public abstract TimeStamp getNewTimeStamp(TimeStamp timeStamp);

	/* compare 2 timestamps */
	public abstract TimeStamp compare(TimeStamp t1, TimeStamp t2) throws TimestampComparisonException;

	public abstract boolean areEqual(TimeStamp t1, TimeStamp t2) throws TimestampComparisonException;
}
