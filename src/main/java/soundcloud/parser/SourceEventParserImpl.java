package soundcloud.parser;

import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soundcloud.event.entity.EventEntity;


public class SourceEventParserImpl implements Parser<EventEntity> {

	private final List<Parser<EventEntity>> parsers;
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static final Parser<EventEntity> DEFAULT = init();

	public SourceEventParserImpl(List<Parser<EventEntity>> parsers) {
		this.parsers = parsers;
	}

	private static Parser<EventEntity> init() {
		return new SourceEventParserImpl(Arrays.asList(new FollowEventParser(),
			new UnfollowEventParser(), new StatusUpdateEventParser(), new PrivateMsgEventParser(),
			new BroadcastEventParser()));
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
