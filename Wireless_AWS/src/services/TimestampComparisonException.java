package services;

public class TimestampComparisonException extends Exception {
	private static final long serialVersionUID = -4671723661781998504L;

	public TimestampComparisonException() {
	}

	public TimestampComparisonException(String message) {
		super(message);
	}

	public TimestampComparisonException(Throwable cause) {
		super(cause);
	}

	public TimestampComparisonException(String message, Throwable cause) {
		super(message, cause);
	}

	public TimestampComparisonException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
