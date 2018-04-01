package soundcloud.user;

import java.nio.channels.SocketChannel;
import java.util.Collection;
import org.jetbrains.annotations.Nullable;

/**
 * @author akt.
 */
public interface UserCache {

	void addUser(String code, UserEntity userEntity);

	@Nullable
	UserEntity getUser(String code);

	Collection<UserEntity> getAllUsers();

	void removeUser(SocketChannel socketChannel);
}
