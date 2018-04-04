package soundcloud;

import java.io.IOException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soundcloud.config.ServerConfig;
import soundcloud.config.ServerConfigImpl;
import soundcloud.event.entity.EventEntity;
import soundcloud.event.executor.EventExecutorDelegate;
import soundcloud.parser.BroadcastEventParser;
import soundcloud.parser.ClientEventParserImpl;
import soundcloud.parser.FollowEventParser;
import soundcloud.parser.Parser;
import soundcloud.parser.PrivateMsgEventParser;
import soundcloud.parser.SourceEventParserImpl;
import soundcloud.parser.StatusUpdateEventParser;
import soundcloud.parser.UnfollowEventParser;
import soundcloud.server.EventProcessor;
import soundcloud.server.NioServer;
import soundcloud.server.event.ServerType;
import soundcloud.user.UserCache;
import soundcloud.user.UserCacheImpl;

/**
 * @author akt.
 */
public class Run {

	private static final Logger logger = LoggerFactory.getLogger(Run.class);

	public static void main(String[] args) throws IOException {
		ServerConfig serverConfig = new ServerConfigImpl();
		logger.debug("Application run with configuration={}", serverConfig);

		UserCache userCache = new UserCacheImpl();
		EventExecutorDelegate eventExecutorDelegate = new EventExecutorDelegate();

		Parser<EventEntity> sourceEventParser = new SourceEventParserImpl(Arrays.asList(new FollowEventParser(),
			new UnfollowEventParser(), new StatusUpdateEventParser(), new PrivateMsgEventParser(),
			new BroadcastEventParser()));
		EventProcessor eventProcessor = new EventProcessor(serverConfig, sourceEventParser,
			new ClientEventParserImpl(), eventExecutorDelegate, userCache);
		new Thread(eventProcessor).start();
		NioServer eventServer = new NioServer(serverConfig.getHostName(), serverConfig.getEventListenerPort(),
			ServerType.EVENT_SOURCE_SERVER, eventProcessor);
		new Thread(eventServer).start();

		NioServer clientServer = new NioServer(serverConfig.getHostName(), serverConfig.getClientListenerPort(),
			ServerType.CLIENTS_SERVER, eventProcessor);
		new Thread(clientServer).start();

		eventExecutorDelegate.build(clientServer, userCache);
	}
}
