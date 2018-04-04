package soundcloud.user;

import java.util.Collection;
import org.jetbrains.annotations.Nullable;


public interface UserCache {

	void addUser(User connectedUser);

	@Nullable
	User getUser(String code);

	Collection<ConnectedUser> getAllConnectedUsers();
}
