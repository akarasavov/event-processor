package soundcloud;

import java.io.IOException;
import soundcloud.config.ServerConfig;
import soundcloud.event.entity.EventEntity;
import soundcloud.event.executor.EventExecutorDelegate;
import soundcloud.parser.ClientEventParserImpl;
import soundcloud.parser.Parser;
import soundcloud.parser.SourceEventParserImpl;
import soundcloud.server.EventProcessor;
import soundcloud.server.NioServer;
import soundcloud.server.ServerSocket;
import soundcloud.server.event.ServerType;
import soundcloud.user.UserCache;
import soundcloud.user.UserCacheImpl;

/**
 * @author akt.
 */
public class Application {

	private final ServerSocket eventServer;
	private final ServerSocket clientServer;
	private final Runnable eventProcessor;

	public Application(ServerConfig serverConfig) throws IOException {
		UserCache userCache = new UserCacheImpl();
		EventExecutorDelegate eventExecutorDelegate = new EventExecutorDelegate();

		Parser<EventEntity> sourceEventParser = SourceEventParserImpl.DEFAULT;
		EventProcessor eventProcessor = new EventProcessor(serverConfig, sourceEventParser,
			new ClientEventParserImpl(), eventExecutorDelegate, userCache);
		this.eventProcessor = eventProcessor;

		this.eventServer = new NioServer(serverConfig.getHostName(), serverConfig.getEventListenerPort(),
			ServerType.EVENT_SOURCE_SERVER, eventProcessor);
		this.clientServer = new NioServer(serverConfig.getHostName(), serverConfig.getClientListenerPort(),
			ServerType.CLIENTS_SERVER, eventProcessor);
		eventExecutorDelegate.build(clientServer, userCache);
	}


	public void start() {
		new Thread(eventProcessor).start();
		new Thread(eventServer).start();
		new Thread(clientServer).start();
	}
}
