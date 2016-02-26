package services;

public class LogicalClock extends ClockService {
	private static final long serialVersionUID = -6856795784872961200L;
	private TimeStamp timeStamp;

	public LogicalClock() {
		super();
		this.timeStamp = TimeStamp.timeStampFactory(ClockTypes.logical);
	}

	@Override
	public synchronized TimeStamp getTimeStamp() {
		return this.timeStamp.clone();
	}

	public synchronized void setTimeStamp(TimeStamp timestamp) {
		this.timeStamp = timestamp;
	}

	@Override
	public synchronized TimeStamp getNewTimeStamp(TimeStamp timeStamp) {
		if (timeStamp == null) {
			this.timeStamp.set_value((int) this.timeStamp.get_value() + 1);
			return this.timeStamp.clone();
		} else {
			this.timeStamp.set_value(Math.max((int) timeStamp.get_value(), (int) this.timeStamp.get_value()) + 1);
			return this.timeStamp.clone();
		}
	}

	@Override
	public TimeStamp compare(TimeStamp t1, TimeStamp t2) throws TimestampComparisonException {
		if (t1 == null || t2 == null) {
			throw new TimestampComparisonException("Can't compare null time-stamps");
		} else {
			throw new TimestampComparisonException("Can't compare two Logical time-stamps");
		}
	}

	@Override
	public boolean areEqual(TimeStamp t1, TimeStamp t2) throws TimestampComparisonException {
		if (t1 == null || t2 == null) {
			throw new TimestampComparisonException("Can't compare null time-stamps");
		}
		return (int) t1.get_value() == (int) t2.get_value();
	}

	public String toString() {
		return String.valueOf((int) this.timeStamp.get_value());
	}

}
