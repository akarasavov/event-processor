package soundcloud.server;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soundcloud.config.ServerConfig;
import soundcloud.event.entity.EventEntity;
import soundcloud.event.executor.EventExecutor;
import soundcloud.parser.Parser;
import soundcloud.server.event.NewMessageEvent;
import soundcloud.server.event.ServerEvent;
import soundcloud.server.event.ServerType;
import soundcloud.user.ConnectedUser;
import soundcloud.user.UserCache;

public class EventProcessor implements Runnable {

	private final ServerConfig serverConfig;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

	private final List<ServerEvent> queue = new LinkedList<>();
	private final List<EventEntity> sourceEvents = new ArrayList<>();
	private final Parser<EventEntity> sourceEventParser;
	private final Parser<String> clientEventParser;
	private final EventExecutor eventExecutor;
	private final UserCache userCache;
	private ScheduledFuture<?> scheduleTask;

	public EventProcessor(ServerConfig serverConfig, Parser<EventEntity> sourceEventParser,
		Parser<String> clientEventParser,
		EventExecutor eventExecutor, UserCache userCache) {
		this.serverConfig = serverConfig;
		this.sourceEventParser = sourceEventParser;
		this.eventExecutor = eventExecutor;
		this.clientEventParser = clientEventParser;
		this.userCache = userCache;
	}

	void newEvent(ServerEvent serverEvent) {
		synchronized (queue) {
			queue.add(serverEvent);
			queue.notify();
		}
	}

	public void run() {
		while (true) {
			ServerEvent serverEvent;
			synchronized (queue) {
				while (queue.isEmpty()) {
					try {
						queue.wait();
					} catch (InterruptedException ignored) {
					}
				}
				serverEvent = queue.remove(0);
			}
			processEvent(serverEvent);
		}
	}

	private void processEvent(ServerEvent serverEvent) {
		NewMessageEvent newMessageEvent = (NewMessageEvent) serverEvent;
		if (serverEvent.getServerSocket().getType() == ServerType.EVENT_SOURCE_SERVER) {
			synchronized (sourceEvents) {
				processNewSourceEvent(newMessageEvent);
			}
		} else if (serverEvent.getServerSocket().getType() == ServerType.CLIENTS_SERVER) {
			processNewClientEvent(newMessageEvent);
		} else {
			logger.warn("Unsupported server type={}", serverEvent.getServerSocket().getType());
		}
	}

	private void processNewSourceEvent(NewMessageEvent serverEvent) {
		if (scheduleTask != null) {
			logger.info("Cancel scheduled task={}", scheduleTask.cancel(false));
		}

		String message = new String(serverEvent.getData(), StandardCharsets.UTF_8);
		logger.info("Processor receive message={}", message);
		List<EventEntity> events = getEventEntity(message);
		sourceEvents.addAll(events);
		int maxEventSourceBatchSize = serverConfig.getMaxEventSourceBatchSize();
		if (sourceEvents.size() >= maxEventSourceBatchSize) {
			executeSourceEvent();
		} else {
			logger.info("Message will be process later. SourceEventsSize={}, maxEventSourceBatchSize={}",
				sourceEvents.size(), maxEventSourceBatchSize);
			int seconds = 5;
			this.scheduleTask = scheduledExecutor.schedule(() -> {
				synchronized (sourceEvents) {
					logger.info("Scheduled task run");
					executeSourceEvent();
				}
			}, seconds, TimeUnit.SECONDS);
			logger.info("Start scheduled task for {} seconds", seconds);
		}
	}

	private void executeSourceEvent() {
		Collections.sort(sourceEvents, (o1, o2) -> o1.getSequence().compareTo(o2.getSequence()));
		sourceEvents.forEach(eventExecutor::execute);
		sourceEvents.clear();
	}

	private void processNewClientEvent(NewMessageEvent serverEvent) {
		String message = new String(serverEvent.getData(), StandardCharsets.UTF_8);
		logger.info("Processor receive message={}", message);
		String userCode = clientEventParser.parse(message);
		ConnectedUser connectedUser = new ConnectedUser(userCode, serverEvent.getSocketChannel());
		logger.info("New user connected.{}", connectedUser);
		userCache.addUser(connectedUser);
	}


	private List<EventEntity> getEventEntity(String message) {
		return Arrays.stream(message.split(ServerConfig.EVENT_SEPARATOR))
			.map(sourceEventParser::parse)
			.filter(Objects::nonNull).collect(Collectors.toList());
	}
}
