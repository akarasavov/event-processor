package soundcloud.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soundcloud.event.entity.EventEntity;
import soundcloud.event.entity.EventType;

/**
 * @author akt.
 */
public class SourceEventParserImpl implements Parser<EventEntity> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public EventEntity parse(String str) {
		String[] tokens = str.split("\\|");
		int length = tokens.length;
		if (tokens.length == 0 || length < 2 || length > 4) {
			logger.warn("can't parse string={}", str);
			return null;
		}
		EventType eventType = EventType.findEventTypeByCode(tokens[1]);
		if (eventType == null) {
			logger.warn("can't parse string={}", str);
			return null;
		}

		EventEntity eventEntity = new EventEntity(str + "\n", eventType);
		if (length > 2) {
			eventEntity.setFromUser(tokens[2]);
		}
		if (length > 3) {
			eventEntity.setToUser(tokens[3]);
		}
		return eventEntity;
	}
}
