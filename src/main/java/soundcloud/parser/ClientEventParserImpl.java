package soundcloud.parser;

import soundcloud.config.ServerConfig;

/**
 * @author akt.
 */
public class ClientEventParserImpl implements Parser<String> {

	@Override
	public String parse(String str) {
		return str.replaceAll(ServerConfig.EVENT_SEPARATOR, "");
	}
}
