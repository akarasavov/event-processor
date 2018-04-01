package soundcloud.event.executor;

import org.jetbrains.annotations.NotNull;
import soundcloud.event.entity.EventEntity;
import soundcloud.server.ServerSocket;
import soundcloud.user.UserCache;

/**
 * @author akt.
 */
class BroadcastExecutor extends AbstractEventExecutor {

	BroadcastExecutor(ServerSocket serverSocket, UserCache userCache) {
		super(serverSocket, userCache);
	}

	@Override
	public void execute(@NotNull EventEntity eventEntity) {
		userCache.getAllUsers().forEach(userEntity ->
			serverSocket.sendMessage(eventEntity.getMessage(), userEntity.getSocketChannel())
		);
	}
}
