package soundcloud.event.executor;

import org.jetbrains.annotations.NotNull;
import soundcloud.event.entity.EventEntity;

/**
 * @author akt.
 */
public interface EventExecutor {

	void execute(@NotNull EventEntity eventEntity);
}
