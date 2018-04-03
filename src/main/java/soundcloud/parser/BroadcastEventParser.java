package soundcloud.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import soundcloud.event.entity.EventEntity;
import soundcloud.event.entity.EventType;

/**
 * @author akt.
 */
public class BroadcastEventParser implements Parser<EventEntity> {

	private final Pattern regex = Pattern.compile("(\\d+)\\|B");

	@Nullable
	@Override
	public EventEntity parse(String str) {
		Matcher matcher = regex.matcher(str);
		if (matcher.matches() && matcher.groupCount() == 1) {
			return new EventEntity(str, matcher.group(1), EventType.BROADCAST);
		}
		return null;
	}
}
