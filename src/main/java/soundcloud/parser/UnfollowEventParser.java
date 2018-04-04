package soundcloud.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import soundcloud.event.entity.EventEntity;
import soundcloud.event.entity.EventType;


public class UnfollowEventParser implements Parser<EventEntity> {

	private final Pattern regex = Pattern.compile("(\\d+)\\|U\\|(\\d+)\\|(\\d+)");

	@Nullable
	@Override
	public EventEntity parse(String str) {
		Matcher matcher = regex.matcher(str);
		if (matcher.matches() && matcher.groupCount() == 3) {
			EventEntity eventEntity = new EventEntity(str, matcher.group(1), EventType.UNFOLLOW);
			eventEntity.setFromUser(matcher.group(2));
			eventEntity.setToUser(matcher.group(3));
			return eventEntity;
		}
		return null;
	}
}
