package soundcloud.event.executor;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soundcloud.event.entity.EventEntity;
import soundcloud.user.UserCache;
import soundcloud.user.UserEntity;

/**
 * @author akt.
 */
public class UnfollowExecutor implements EventExecutor {

	private Logger logger = LoggerFactory.getLogger(UnfollowExecutor.class);
	private final UserCache userCache;

	public UnfollowExecutor(UserCache userCache) {
		this.userCache = userCache;
	}

	@Override
	public void execute(@NotNull EventEntity eventEntity) {
		UserEntity toUser = userCache.getUser(eventEntity.getToUser());
		if (toUser != null) {
			toUser.removeFollower(eventEntity.getFromUser());
		} else {
			logger.warn("Event={} can't be execute, because there is no toUser in cache", eventEntity);
		}
	}
}