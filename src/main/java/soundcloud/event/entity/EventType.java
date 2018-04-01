package soundcloud.event.entity;

import org.jetbrains.annotations.Nullable;

/**
 * @author akt.
 */
public enum EventType {
	FOLLOW("F"),
	UNFOLLOW("U"),
	BROADCAST("B"),
	PRIVATE_MSG("P"),
	STATUS_UPDATE("S");

	String code;

	EventType(String code) {
		this.code = code;
	}

	@Nullable
	public static EventType findEventTypeByCode(String code) {
		for (EventType eventType : values()) {
			if (eventType.code.equals(code)) {
				return eventType;
			}
		}
		return null;
	}
}
