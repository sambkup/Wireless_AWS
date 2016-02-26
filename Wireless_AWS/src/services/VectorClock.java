package services;

public class VectorClock extends ClockService {
	private static final long serialVersionUID = -5261619615072061910L;
	TimeStamp currentTimeStamp;
	int myIndex;
	int size;

	/*
	 * instantiation size - the size to make the vector clock index - the index
	 * of hte vector clock that belongs to me
	 */
	public VectorClock(int size, int index) {
		this.size = size;
		// make sure size is bigger than 0
		// make sure that 0<=index<size
		if (size > 0 && index >= 0 && index < size) {
			// create the time stamp
			this.currentTimeStamp = TimeStamp.timeStampFactory(ClockTypes.vector, size);

			// let me know which element of the vector clock is me
			this.myIndex = index;
		} else {
			System.out.println("Cannot create a vector clock with length<1 and myIndex<0");
		}
	}

	@Override
	public synchronized TimeStamp getTimeStamp() {
		return this.currentTimeStamp.clone();
	}

	public synchronized void setTimeStamp(TimeStamp timestamp) {
		this.currentTimeStamp = timestamp;
	}

	@Override
	public synchronized TimeStamp getNewTimeStamp(TimeStamp newTimeStamp) {
		/*
		 * if input is null - just increment my index of timestamp if input is a
		 * value - compute max() and update in both cases, return a timestamp
		 */

		if (newTimeStamp == null) {
			// if no new timestamp specified, just increment my timestamp
			return incrementTimeStamp().clone();
		} else {
			// if there is a timestamp specified, update based on the rules

			// extract the int[] arrays for mine and the new one
			int[] current = (int[]) this.currentTimeStamp.get_value();
			int[] received = (int[]) newTimeStamp.get_value();
			// compute the component-wise maximum
			this.currentTimeStamp.set_value(componentMax(current, received));
			incrementTimeStamp();
			return this.currentTimeStamp.clone();
		}
	}

	private int[] componentMax(int[] t1, int[] t2) {

		int arr_length = t1.length;

		// make sure they are the same length
		if (arr_length != t2.length) {
			System.out.println("Error: Trying to do componentMax on different sized arrays");
			return null;
		}

		else {
			for (int i = 0; i < arr_length; i++) {
				if (t2[i] > t1[i]) {
					t1[i] = t2[i];
				}
			}
		}
		return t1;
	}

	private TimeStamp incrementTimeStamp() {
		// get int array from the vector timestamp
		int[] time = (int[]) this.currentTimeStamp.get_value();
		// update the value from my time
		time[this.myIndex] = time[this.myIndex] + 1;
		// set the new time
		this.currentTimeStamp.set_value(time);
		// return the updated timestamp
		return this.currentTimeStamp;
	}

	@Override
	public TimeStamp compare(TimeStamp t1, TimeStamp t2) throws TimestampComparisonException {
		if (t1 == null || t2 == null) {
			throw new TimestampComparisonException("Can't compare null time-stamps");
		}
		int[] time1 = (int[]) t1.get_value();
		int[] time2 = (int[]) t2.get_value();

		boolean equal = checkEquals(time1, time2);
		if (equal) {
			return t1;
		}

		boolean LE = checkLE(time1, time2);
		if (LE) {
			// since we know LE and notEqual, we can conclude t1 is before t2
			return t1;
		}

		boolean GE = checkLE(time2, time1);
		if (GE) {
			// since we know GE and notEqual, we can conclude t2 is before t1
			return t2;
		}

		return null;
	}

	/*
	 * Check if t1=t2 returns true if true, false if not on error, returns null
	 */
	private boolean checkEquals(int[] t1, int[] t2) {
		int arr_length = t1.length;

		// make sure they are the same length
		if (arr_length != t2.length) {
			return false;
		}
		for (int i = 0; i < arr_length; i++) {
			if (t1[i] != t2[i]) {
				return false;
			}
		}
		return true;
	}

	/*
	 * Check if t1<=t2 returns true if true, false if not on error, returns null
	 */
	@SuppressWarnings("null")
	private boolean checkLE(int[] t1, int[] t2) {
		int arr_length = t1.length;

		// make sure they are the same length
		if (arr_length != t2.length) {
			System.out.println("Error: Trying to do checkEquals on different sized arrays");
			System.out.println("t1 = "+Integer.toString(t1.length)+ " t2 = "+Integer.toString(t2.length));
			// TODO: instead of returning null, consider throwing an exception
			return (Boolean) null;
		}
		for (int i = 0; i < arr_length; i++) {
			if (t1[i] > t2[i]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean areEqual(TimeStamp t1, TimeStamp t2) throws TimestampComparisonException {
		if (t1 == null || t2 == null) {
			throw new TimestampComparisonException("Can't compare null time-stamps");
		}
		int[] time1 = (int[]) t1.get_value();
		int[] time2 = (int[]) t2.get_value();
		return checkEquals(time1, time2);
	}

}
