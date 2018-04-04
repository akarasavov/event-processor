package soundcloud;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soundcloud.config.ServerConfig;
import soundcloud.config.ServerConfigImpl;

/**
 * @author akt.
 */
public class Run {

	private static final Logger logger = LoggerFactory.getLogger(Run.class);

	public static void main(String[] args) throws IOException {
		ServerConfig serverConfig = new ServerConfigImpl();
		logger.info("Application run with configuration={}", serverConfig);

		new Application(serverConfig).start();
	}
}
