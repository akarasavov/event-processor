package soundcloud.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import soundcloud.event.entity.EventEntity;
import soundcloud.event.entity.EventType;

/**
 * @author akt.
 */
public class StatusUpdateEventParser implements Parser<EventEntity> {

	private final Pattern regex = Pattern.compile("(\\d+)\\|S\\|(\\d+)");

	@Nullable
	@Override
	public EventEntity parse(String str) {
		Matcher matcher = regex.matcher(str);
		if (matcher.matches() && matcher.groupCount() == 2) {
			EventEntity eventEntity = new EventEntity(str, matcher.group(1), EventType.STATUS_UPDATE);
			eventEntity.setFromUser(matcher.group(2));
			return eventEntity;
		}
		return null;
	}
}
