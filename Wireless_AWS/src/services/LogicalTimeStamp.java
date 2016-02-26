package services;

public class LogicalTimeStamp extends TimeStamp {
	private static final long serialVersionUID = -5907909481165789707L;
	private int value;

	public LogicalTimeStamp() {
		super();
	}

	@Override
	public Object get_value() {
		return value;
	}

	@Override
	public void set_value(Object newValue) {
		this.value = (int) newValue;
	}

	@Override
	public TimeStamp clone() {
		LogicalTimeStamp clone = new LogicalTimeStamp();
		clone.set_value(this.value);
		return clone;
	}

	@Override
	public String toString() {
		return String.valueOf(this.value);
	}

}
