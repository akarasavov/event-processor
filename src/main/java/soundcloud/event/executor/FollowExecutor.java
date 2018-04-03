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
class FollowExecutor extends AbstractEventExecutor {

	public FollowExecutor(ServerSocket serverSocket, UserCache userCache) {
		super(serverSocket, userCache);
	}

	@Override
	public void execute(@NotNull EventEntity eventEntity) {
		String toUserCode = eventEntity.getToUser();
		User toUser = userCache.getUser(toUserCode);
		if (toUser != null) {
			toUser.addFollower(eventEntity.getFromUser());
			if (toUser.isConnected()) {
				byte[] data = eventEntity.getMessage().getBytes(StandardCharsets.UTF_8);
				serverSocket.send(((ConnectedUser) toUser).getSocketChannel(), data);
			}
		} else {
			User newUser = new User(toUserCode);
			newUser.addFollower(eventEntity.getFromUser());
			userCache.addUser(newUser);
		}
	}
}
