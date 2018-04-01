package soundcloud.parser;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import soundcloud.entity.Action;
import soundcloud.entity.ActionType;

/**
 * @author akt.
 */
public class ActionParserImpl implements ActionParser {

	@Override
	public Action parse(String str) {
		String[] tokens = str.split("\\W");
		List<String> filteredToken = Arrays.stream(tokens).filter(token -> !token.isEmpty()).collect(Collectors.toList());
		if (filteredToken.isEmpty() || filteredToken.size() < 2 || filteredToken.size() > 4) {
			return null;
		}
		ActionType actionType = ActionType.findActionTypeByCode(filteredToken.get(1));
		if (actionType == null) {
			return null;
		}

		Action action = new Action(str, actionType);
		if (filteredToken.size() > 2) {
			action.setFromUser(filteredToken.get(2));
		}
		if (filteredToken.size() > 3) {
			action.setToUser(filteredToken.get(3));
		}
		return action;
	}
}
