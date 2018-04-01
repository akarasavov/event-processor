package soundcloud.user;

import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author akt.
 */
public class UserCacheImpl implements UserCache {

	private Logger logger = LoggerFactory.getLogger(UserCacheImpl.class);
	private final Map<String, UserEntity> codeUserEntityMap = new ConcurrentHashMap<>();
	private final Map<SocketChannel, String> socketChannelStringMap = new ConcurrentHashMap<>();

	@Override
	public void addUser(String code, UserEntity userEntity) {
		logger.info("User added. User={}", userEntity);
		codeUserEntityMap.put(code, userEntity);
		socketChannelStringMap.put(userEntity.getSocketChannel(), code);
	}

	@Nullable
	@Override
	public UserEntity getUser(String code) {
		return codeUserEntityMap.get(code);
	}

	@Override
	public Collection<UserEntity> getAllUsers() {
		return codeUserEntityMap.values();
	}

	@Override
	public void removeUser(SocketChannel socketChannel) {
		String code = socketChannelStringMap.remove(socketChannel);
		if (code != null) {
			codeUserEntityMap.remove(code);
		} else {
			logger.warn("Passed SocketChannel={} is not stored in cache", socketChannel);
		}
	}

}
