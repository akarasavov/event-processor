package soundcloud;

import java.util.Arrays;

/**
 * @author akt.
 */
public class Run {

	private final static String CRLF = "\r\n";

	public static void main(String[] args) {
		String str = "asdasd\nasdasdccccc\n";
		Arrays.stream(str.split(CRLF)).forEach(System.out::println);
	}
}
