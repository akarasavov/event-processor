package soundcloud.event.executor;

import java.nio.charset.StandardCharsets;
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
		userCache.getAllUsers().forEach(userEntity -> {
				byte[] data = eventEntity.getMessage().getBytes(StandardCharsets.UTF_8);
				serverSocket.send(userEntity.getSocketChannel(), data);
			}
		);
	}
}
