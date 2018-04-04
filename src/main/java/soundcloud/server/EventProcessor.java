package soundcloud.server;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
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

public class EventProcessor implements CancelableRunnable {

	private final ServerConfig serverConfig;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

	private final List<ServerEvent> serverEvents = new LinkedList<>();
	private final List<EventEntity> sourceEvents = new ArrayList<>();
	private final Parser<EventEntity> sourceEventParser;
	private final Parser<String> clientEventParser;
	private final EventExecutor eventExecutor;
	private final UserCache userCache;
	private ScheduledFuture<?> scheduleTask;

	private volatile boolean shutdown;
	private volatile boolean killed;

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
		synchronized (serverEvents) {
			serverEvents.add(serverEvent);
			serverEvents.notify();
		}
	}

	public void run() {
		while (true) {
			ServerEvent serverEvent;
			synchronized (serverEvents) {
				while (serverEvents.isEmpty()) {
					try {
						serverEvents.wait();
						if (shutdown) {
							killed = true;
							logger.info("EventProcessor is killed");
						}
					} catch (InterruptedException ignored) {
					}
				}
				serverEvent = serverEvents.remove(0);
			}
			processEvent(serverEvent);
		}
	}

	private void processEvent(ServerEvent serverEvent) {
		NewMessageEvent newMessageEvent = (NewMessageEvent) serverEvent;
		if (serverEvent.getServerSocket().getServerType() == ServerType.EVENT_SOURCE_SERVER) {
			synchronized (sourceEvents) {
				processNewSourceEvent(newMessageEvent);
			}
		} else if (serverEvent.getServerSocket().getServerType() == ServerType.CLIENTS_SERVER) {
			processNewClientEvent(newMessageEvent);
		} else {
			logger.warn("Unsupported server type={}", serverEvent.getServerSocket().getServerType());
		}
	}

	private void processNewSourceEvent(NewMessageEvent serverEvent) {
		if (scheduleTask != null) {
			logger.debug("Cancel scheduled task={}", scheduleTask.cancel(false));
		}

		String message = new String(serverEvent.getData(), StandardCharsets.UTF_8);
		List<EventEntity> events = getEventEntity(message);
		logger.debug("Processor receive packageSize={}, message={}", events.size(), message);
		sourceEvents.addAll(events);
		if (sourceEvents.size() >= serverConfig.getMessageBufferSize()) {
			executeSourceEvent();
		} else {
			logger.debug("Message will be process later. SourceEventsSize={}, accumulatorSize={}", sourceEvents.size(),
				serverConfig.getMessageBufferSize());
			this.scheduleTask = scheduledExecutor.schedule(() -> {
				synchronized (sourceEvents) {
					logger.debug("Scheduled task run");
					executeSourceEvent();
				}
			}, serverConfig.getAccumulateSeconds(), TimeUnit.MILLISECONDS);
			logger.debug("Start scheduled task for {} seconds", serverConfig.getAccumulateSeconds());
		}
	}

	private void executeSourceEvent() {
		Collections.sort(sourceEvents, (o1, o2) -> o1.getSequence().compareTo(o2.getSequence()));
		logger.debug("Executing message with size={}, message={}", sourceEvents.size(), sourceEvents);
		sourceEvents.forEach(eventExecutor::execute);
		sourceEvents.clear();
	}

	private void processNewClientEvent(NewMessageEvent serverEvent) {
		String message = new String(serverEvent.getData(), StandardCharsets.UTF_8);
		logger.debug("Processor receive message={}", message);
		String userCode = clientEventParser.parse(message);
		ConnectedUser connectedUser = new ConnectedUser(userCode, serverEvent.getSocketChannel());
		logger.debug("New user connected.{}", connectedUser);
		userCache.addUser(connectedUser);
	}


	private List<EventEntity> getEventEntity(String message) {
		return Arrays.stream(message.split(ServerConfig.EVENT_SEPARATOR))
			.map(sourceEventParser::parse)
			.filter(Objects::nonNull).collect(Collectors.toList());
	}

	@Override
	public Callable<Boolean> shutdown() {
		shutdown = true;
		return () -> {
			while (!killed) {
				synchronized (serverEvents) {
					serverEvents.notify();
				}
				Thread.sleep(100);
			}
			return true;
		};
	}

}
