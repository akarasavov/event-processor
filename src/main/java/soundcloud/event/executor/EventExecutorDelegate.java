package soundcloud.event.executor;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soundcloud.event.entity.EventEntity;
import soundcloud.event.entity.EventType;
import soundcloud.server.ServerSocket;
import soundcloud.user.UserCache;

/**
 * @author akt.
 */
public class EventExecutorDelegate implements EventExecutor {

	private Logger logger = LoggerFactory.getLogger(EventExecutorDelegate.class);
	private final Map<EventType, EventExecutor> eventExecutorMap;


	public EventExecutorDelegate(ServerSocket serverSocket, UserCache userCache) {
		this.eventExecutorMap = new HashMap<>();
		eventExecutorMap.put(EventType.FOLLOW, new FollowExecutor(serverSocket, userCache));
		eventExecutorMap.put(EventType.UNFOLLOW, new UnfollowExecutor(userCache));
		eventExecutorMap.put(EventType.BROADCAST, new BroadcastExecutor(serverSocket, userCache));
		eventExecutorMap.put(EventType.PRIVATE_MSG, new PrivateMessage(serverSocket, userCache));
		eventExecutorMap.put(EventType.STATUS_UPDATE, new StatusUpdateExecutor(serverSocket, userCache));
	}

	@Override
	public void execute(@NotNull EventEntity eventEntity) {
		EventExecutor eventExecutor = eventExecutorMap.get(eventEntity.getEventType());
		if (eventExecutor == null) {
			logger.error("Register Event Executor for eventType={}", eventEntity.getEventType());
			throw new IllegalArgumentException(String.format("EventEntity=%s can't be executed", eventEntity));
		} else {
			eventExecutor.execute(eventEntity);
		}
	}
}
