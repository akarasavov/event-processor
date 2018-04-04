package soundcloud.event.executor;

import java.nio.channels.SocketChannel;
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
			String message = eventEntity.getMessage();
			byte[] data = message.getBytes(StandardCharsets.UTF_8);
			SocketChannel socketChannel = ((ConnectedUser) toUser).getSocketChannel();
			logger.info("Send PrivateMsg. message={}, user={}", message, toUser);
			serverSocket.send(socketChannel, data);
		} else {
			logToUserProblem(eventEntity);
		}
	}
}
