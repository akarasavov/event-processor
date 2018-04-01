package soundcloud.action.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author akt.
 */
public class EventEntity {

	private final String message;
	private final EventType eventType;
	private String fromUser;
	private String toUser;

	public EventEntity(String message, EventType eventType) {
		this.message = message;
		this.eventType = eventType;
	}

	public void setFromUser(String fromUser) {
		this.fromUser = fromUser;
	}

	public void setToUser(String toUser) {
		this.toUser = toUser;
	}

	public String getMessage() {
		return message;
	}


	@NotNull
	public EventType getEventType() {
		return eventType;
	}

	@Nullable
	public String getFromUser() {
		return fromUser;
	}

	@NotNull
	public String getToUser() {
		return toUser;
	}

	@Override
	public String toString() {
		return "EventEntity{" +
			"message='" + message + '\'' +
			", eventType=" + eventType +
			", fromUser='" + fromUser + '\'' +
			", toUser='" + toUser + '\'' +
			'}';
	}
}