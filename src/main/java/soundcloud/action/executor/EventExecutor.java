package soundcloud.action.executor;

import org.jetbrains.annotations.NotNull;
import soundcloud.action.entity.EventEntity;

/**
 * @author akt.
 */
public interface EventExecutor {

	void execute(@NotNull EventEntity eventEntity);
}
