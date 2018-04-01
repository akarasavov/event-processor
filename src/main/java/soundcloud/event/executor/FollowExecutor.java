package soundcloud.event.executor;

import org.jetbrains.annotations.NotNull;
import soundcloud.event.entity.EventEntity;
import soundcloud.server.ServerSocket;
import soundcloud.user.UserCache;
import soundcloud.user.UserEntity;

/**
 * @author akt.
 */
class FollowExecutor extends AbstractEventExecutor {

	public FollowExecutor(ServerSocket serverSocket, UserCache userCache) {
		super(serverSocket, userCache);
	}

	@Override
	public void execute(@NotNull EventEntity eventEntity) {
		UserEntity toUser = userCache.getUser(eventEntity.getToUser());
		if (toUser != null) {
			toUser.addFollower(eventEntity.getFromUser());
			serverSocket.sendMessage(eventEntity.getMessage(), toUser.getSocketChannel());
		} else {
			logToUserProblem(eventEntity);
		}
	}
}
