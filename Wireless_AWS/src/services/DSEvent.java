package services;

public class DSEvent {

	private TimeStamp eventTimeStamp;
	private String eventKind;
	private Object eventObject;

	public DSEvent(TimeStamp timeStamp, String kind, Object eventObject) {
		this.eventTimeStamp = timeStamp;
		this.eventKind = kind;
		this.eventObject = eventObject;
	}

	public TimeStamp getEventTimeStamp() {
		return eventTimeStamp;
	}

	public void setEventTimeStamp(TimeStamp eventTimeStamp) {
		this.eventTimeStamp = eventTimeStamp;
	}

	public String getEventKind() {
		return eventKind;
	}

	public void setEventKind(String eventKind) {
		this.eventKind = eventKind;
	}

	public Object getEventObject() {
		return eventObject;
	}

	public void setEventObject(Object eventObject) {
		this.eventObject = eventObject;
	}

	public String toString() {
		return String.format("Event Kind: %s, Timestamp: %s, Data: %s", this.eventKind, this.eventTimeStamp.toString(),
				this.eventObject.toString());
	}

}
