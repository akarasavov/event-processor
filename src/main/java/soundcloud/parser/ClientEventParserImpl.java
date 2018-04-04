package soundcloud.parser;

import soundcloud.ApplicationConstant;

/**
 * @author akt.
 */
public class ClientEventParserImpl implements Parser<String> {

	@Override
	public String parse(String str) {
		return str.replaceAll(ApplicationConstant.EVENT_SEPARATOR, "");
	}
}
