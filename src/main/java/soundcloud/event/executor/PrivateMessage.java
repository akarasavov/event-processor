package soundcloud.event.executor;

import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;
import soundcloud.event.entity.EventEntity;
import soundcloud.server.ServerSocket;
import soundcloud.user.UserCache;
import soundcloud.user.UserEntity;

/**
 * @author akt.
 */
public class PrivateMessage extends AbstractEventExecutor {

	public PrivateMessage(ServerSocket serverSocket, UserCache userCache) {
		super(serverSocket, userCache);
	}

	@Override
	public void execute(@NotNull EventEntity eventEntity) {
		UserEntity toUser = userCache.getUser(eventEntity.getToUser());
		if (toUser != null) {
			byte[] data = eventEntity.getMessage().getBytes(StandardCharsets.UTF_8);
			serverSocket.send(toUser.getSocketChannel(), data);
		} else {
			logToUserProblem(eventEntity);
		}
	}
}
