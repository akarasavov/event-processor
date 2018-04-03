package soundcloud.event.executor;

import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;
import soundcloud.event.entity.EventEntity;
import soundcloud.server.ServerSocket;
import soundcloud.user.ConnectedUser;
import soundcloud.user.User;
import soundcloud.user.UserCache;

/**
 * @author akt.
 */
public class StatusUpdateExecutor extends AbstractEventExecutor {

	public StatusUpdateExecutor(ServerSocket serverSocket, UserCache userCache) {
		super(serverSocket, userCache);
	}

	@Override
	public void execute(@NotNull EventEntity eventEntity) {
		User fromUser = userCache.getUser(eventEntity.getFromUser());
		if (fromUser != null) {
			fromUser.getFollowers().forEach(followerCode -> {
				User toUser = userCache.getUser(followerCode);
				if (toUser != null && toUser.isConnected()) {
					byte[] data = eventEntity.getMessage().getBytes(StandardCharsets.UTF_8);
					serverSocket.send(((ConnectedUser) toUser).getSocketChannel(), data);
				} else {
					logger.warn("Can't notify user with code={}, because it is not in the cache", followerCode);
				}
			});
		} else {
			logger.warn("Event={} can't be execute, because there is no fromUser in cache", eventEntity);
		}
	}
}
