package soundcloud.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author akt.
 */
public class Action {

	private final String message;
	private final ActionType actionType;
	private String fromUser;
	private String toUser;

	public Action(String message, ActionType actionType) {
		this.message = message;
		this.actionType = actionType;
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
	public ActionType getActionType() {
		return actionType;
	}

	@Nullable
	public String getFromUser() {
		return fromUser;
	}

	@NotNull
	public String getToUser() {
		return toUser;
	}
}
