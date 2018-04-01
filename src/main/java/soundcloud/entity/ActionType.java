package soundcloud.entity;

import org.jetbrains.annotations.Nullable;

/**
 * @author akt.
 */
public enum ActionType {
	FOLLOW("F"),
	UNFOLLOW("U"),
	BROADCAST("B"),
	PRIVATE_MSG("P"),
	STATUS_UPDATE("S");

	String code;

	ActionType(String code) {
		this.code = code;
	}

	@Nullable
	public static ActionType findActionTypeByCode(String code) {
		for (ActionType actionType : values()) {
			if (actionType.code.equals(code)) {
				return actionType;
			}
		}
		return null;
	}
}
