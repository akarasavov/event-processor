package soundcloud.user;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author akt.
 */
public class UserCacheImpl implements UserCache {

	private Logger logger = LoggerFactory.getLogger(UserCacheImpl.class);
	private final Map<String, User> codeUserEntityMap = new HashMap<>();

	@Override
	public void addUser(User user) {
		codeUserEntityMap.put(user.getUserCode(), user);
	}

	@Nullable
	@Override
	public User getUser(String code) {
		return codeUserEntityMap.get(code);
	}

	@Override
	public Collection<ConnectedUser> getAllConnectedUsers() {
		return codeUserEntityMap.values().stream().filter(User::isConnected)
			.map(user -> (ConnectedUser) user)
			.collect(Collectors.toList());
	}

}
