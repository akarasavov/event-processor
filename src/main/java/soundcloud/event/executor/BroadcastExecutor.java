package soundcloud.event.executor;

import java.nio.channels.SocketChannel;
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
		userCache.getAllConnectedUsers().forEach(userEntity -> {
			String message = eventEntity.getMessage();
			SocketChannel socketChannel = userEntity.getSocketChannel();
			logger.info("Send Broadcast notification message={} to address={}", message, socketChannel);
			byte[] data = message.getBytes(StandardCharsets.UTF_8);
			serverSocket.send(socketChannel, data);
			}
		);
	}
}
