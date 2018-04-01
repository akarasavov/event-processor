package soundcloud.parser;

import org.jetbrains.annotations.Nullable;
import soundcloud.entity.Action;

/**
 * @author akt.
 */
public interface ActionParser {

	/**
	 * @return - null if passed string can't be parsed, otherwise return parsed action
	 */
	@Nullable
	Action parse(String str);
}
