package soundcloud.entity;

/**
 * @author akt.
 */
public class Action {

	private final String message;
	private final String sequence;
	private final ActionType actionType;
	private final String fromUser;
	private final String toUser;

	public Action(String message, String sequence, ActionType actionType, String fromUser, String toUser) {
		this.message = message;
		this.sequence = sequence;
		this.actionType = actionType;
		this.fromUser = fromUser;
		this.toUser = toUser;
	}

	public String getMessage() {
		return message;
	}

	public String getSequence() {
		return sequence;
	}

	public ActionType getActionType() {
		return actionType;
	}

	public String getFromUser() {
		return fromUser;
	}

	public String getToUser() {
		return toUser;
	}
}
