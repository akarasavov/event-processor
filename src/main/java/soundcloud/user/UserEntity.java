package soundcloud.user;

import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

/**
 * @author akt.
 */
public class UserEntity {

	private final SocketChannel socketChannel;
	private final Set<UserEntity> followers = new HashSet<>();

	public UserEntity(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	public void addFollower(UserEntity userEntity) {
		followers.add(userEntity);
	}

	public void removeFollower(UserEntity userEntity) {
		followers.remove(userEntity);
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public Set<UserEntity> getFollowers() {
		return followers;
	}
}
