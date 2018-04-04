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
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soundcloud.ApplicationConstant;
import soundcloud.event.entity.EventEntity;
import soundcloud.event.executor.EventExecutor;
import soundcloud.parser.Parser;
import soundcloud.server.event.NewMessageEvent;
import soundcloud.server.event.ServerEvent;
import soundcloud.user.ConnectedUser;
import soundcloud.user.UserCache;

public class EventProcessor implements Runnable {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

	private final List<ServerEvent> queue = new LinkedList<>();
	private final List<EventEntity> sourceEvents = new ArrayList<>();
	private final Parser<EventEntity> sourceEventParser;
	private final Parser<String> clientEventParser;
	private final EventExecutor eventExecutor;
	private final UserCache userCache;
	private final int maxEventSourceBatchSize;

	public EventProcessor(int maxEventSourceBatchSize, Parser<EventEntity> sourceEventParser,
		Parser<String> clientEventParser,
		EventExecutor eventExecutor, UserCache userCache) {
		this.maxEventSourceBatchSize = maxEventSourceBatchSize;
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
		if (serverEvent.getServerSocket().getType() == 0 && serverEvent instanceof NewMessageEvent) {
			processNewSourceEvent((NewMessageEvent) serverEvent);
		} else if (serverEvent.getServerSocket().getType() == 1 && serverEvent instanceof NewMessageEvent) {
			processNewClientEvent((NewMessageEvent) serverEvent);
		} else {
			logger.info("EventProcessor receive unsupported message={}", serverEvent);
		}
	}

	private void processNewSourceEvent(NewMessageEvent serverEvent) {
		String message = new String(serverEvent.getData(), StandardCharsets.UTF_8);
		logger.info("Processor receive message={}", message);
		List<EventEntity> events = getEventEntity(message);
		sourceEvents.addAll(events);
		if (sourceEvents.size() >= maxEventSourceBatchSize) {
			executeSourceEvent();
		} else {
			logger.info("Message will be process later. SourceEventsSize={}, maxEventSourceBatchSize={}",
				sourceEvents.size(), maxEventSourceBatchSize);
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
		return Arrays.stream(message.split(ApplicationConstant.EVENT_SEPARATOR))
			.map(sourceEventParser::parse)
			.filter(Objects::nonNull).collect(Collectors.toList());
	}
}
