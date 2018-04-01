package soundcloud.service;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soundcloud.action.entity.EventEntity;
import soundcloud.action.entity.EventType;
import soundcloud.action.executor.EventExecutor;
import soundcloud.action.executor.FollowExecutor;

/**
 * @author akt.
 */
public class EventExecutorService implements EventExecutor {

	private Logger logger = LoggerFactory.getLogger(EventExecutorService.class);
	private final Map<EventType, EventExecutor> eventExecutorMap;


	public EventExecutorService() {
		this.eventExecutorMap = new HashMap<>();
		eventExecutorMap.put(EventType.FOLLOW, new FollowExecutor());
		//TODO - register all executors;
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
