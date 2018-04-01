package soundcloud.parser;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import soundcloud.action.entity.EventEntity;
import soundcloud.action.entity.EventType;

/**
 * @author akt.
 */
public class SourceEventParserImpl implements Parser<EventEntity> {

	@Override
	public EventEntity parse(String str) {
		String[] tokens = str.split("\\W");
		List<String> filteredToken = Arrays.stream(tokens).filter(token -> !token.isEmpty())
			.collect(Collectors.toList());
		if (filteredToken.isEmpty() || filteredToken.size() < 2 || filteredToken.size() > 4) {
			return null;
		}
		EventType eventType = EventType.findEventTypeByCode(filteredToken.get(1));
		if (eventType == null) {
			return null;
		}

		EventEntity eventEntity = new EventEntity(str, eventType);
		if (filteredToken.size() > 2) {
			eventEntity.setFromUser(filteredToken.get(2));
		}
		if (filteredToken.size() > 3) {
			eventEntity.setToUser(filteredToken.get(3));
		}
		return eventEntity;
	}
}
