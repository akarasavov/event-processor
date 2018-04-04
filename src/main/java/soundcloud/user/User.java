package soundcloud.user;

import java.util.HashSet;
import java.util.Set;


public class User {

	private final String userCode;
	private final Set<String> followers = new HashSet<>();

	public User(String userCode) {
		this.userCode = userCode;
	}

	public void addFollower(String follower) {
		followers.add(follower);
	}

	public boolean removeFollower(String follower) {
		return followers.remove(follower);
	}

	public Set<String> getFollowers() {
		return followers;
	}

	public boolean isConnected() {
		return false;
	}

	public String getUserCode() {
		return userCode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		User that = (User) o;

		return userCode.equals(that.userCode);

	}

	@Override
	public int hashCode() {
		return userCode.hashCode();
	}

	@Override
	public String toString() {
		return "User{" +
			"userCode='" + userCode + '\'' +
			", followers=" + followers +
			'}';
	}
}
