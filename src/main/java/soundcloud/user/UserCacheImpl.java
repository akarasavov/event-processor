package soundcloud.user;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

/**
 * @author akt.
 */
public class UserCacheImpl {

	private final Map<String, UserEntity> codeUserEntityMap = new HashMap<>();

	public void addUser(String code, UserEntity userEntity) {
		codeUserEntityMap.put(code, userEntity);
	}

	@Nullable
	public UserEntity getUser(String code) {
		return codeUserEntityMap.get(code);
	}

	public void removeUser(String code) {
		codeUserEntityMap.remove(code);
	}

}
