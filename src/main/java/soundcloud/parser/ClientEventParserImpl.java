package soundcloud.parser;

/**
 * @author akt.
 */
public class ClientEventParserImpl implements Parser<String> {

	@Override
	public String parse(String str) {
		return str.replaceAll("\r\n", "");
	}
}
