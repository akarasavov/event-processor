package soundcloud.parser;

import soundcloud.entity.Action;

/**
 * @author akt.
 */
public interface ActionParser {

	Action parse(String str);
}
