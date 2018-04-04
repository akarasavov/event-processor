package soundcloud.event.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soundcloud.event.entity.EventEntity;
import soundcloud.server.ServerSocket;
import soundcloud.user.UserCache;


public abstract class AbstractEventExecutor implements EventExecutor {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	final ServerSocket serverSocket;
	final UserCache userCache;

	AbstractEventExecutor(ServerSocket serverSocket, UserCache userCache) {
		this.serverSocket = serverSocket;
		this.userCache = userCache;
	}

	final void logToUserProblem(EventEntity eventEntity) {
		logger.warn("Event={} can't be execute, because there is no toUser in cache", eventEntity);
	}
}
