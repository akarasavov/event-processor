package soundcloud.user;

import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

/**
 * @author akt.
 */
public class UserEntity {

	private final SocketChannel socketChannel;
	private final String userCode;
	private final Set<String> followers = new HashSet<>();

	public UserEntity(String userCode, SocketChannel socketChannel) {
		this.userCode = userCode;
		this.socketChannel = socketChannel;
	}

	public void addFollower(String follower) {
		followers.add(follower);
	}

	public void removeFollower(String follower) {
		followers.remove(follower);
	}

	public Set<String> getFollowers() {
		return followers;
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		UserEntity that = (UserEntity) o;

		return userCode.equals(that.userCode);

	}

	@Override
	public int hashCode() {
		return userCode.hashCode();
	}

	@Override
	public String toString() {
		return "UserEntity{" +
			"userCode='" + userCode + '\'' +
			'}';
	}
}
