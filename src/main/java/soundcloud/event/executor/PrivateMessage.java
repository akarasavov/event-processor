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
public class PrivateMessage extends AbstractEventExecutor {

	public PrivateMessage(ServerSocket serverSocket, UserCache userCache) {
		super(serverSocket, userCache);
	}

	@Override
	public void execute(@NotNull EventEntity eventEntity) {
		User toUser = userCache.getUser(eventEntity.getToUser());
		if (toUser != null && toUser.isConnected()) {
			byte[] data = eventEntity.getMessage().getBytes(StandardCharsets.UTF_8);
			serverSocket.send(((ConnectedUser) toUser).getSocketChannel(), data);
		} else {
			logToUserProblem(eventEntity);
		}
	}
}
