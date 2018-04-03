package soundcloud.parser;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soundcloud.event.entity.EventEntity;

/**
 * @author akt.
 */
public class SourceEventParserImpl implements Parser<EventEntity> {

	private final List<Parser<EventEntity>> parsers;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public SourceEventParserImpl(List<Parser<EventEntity>> parsers) {
		this.parsers = parsers;
	}

	@Override
	public EventEntity parse(String str) {
		for (Parser<EventEntity> parser : parsers) {
			EventEntity eventEntity = parser.parse(str);
			if (eventEntity != null) {
				return eventEntity;
			}
		}
		logger.warn("Message={} can't be parsed");
		return null;
	}
}
