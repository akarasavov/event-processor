package soundcloud.parser;

import org.jetbrains.annotations.Nullable;

/**
 * @author akt.
 */
public interface Parser<T> {

	/**
	 * @return - null if passed string can't be parsed, otherwise return parsed object
	 */
	@Nullable
	T parse(String str);
}
