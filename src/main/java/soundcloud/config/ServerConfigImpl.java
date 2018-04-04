package soundcloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author akt.
 */
public class ServerConfigImpl implements ServerConfig {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private String hostName = "127.0.0.1";
	private final int messageBufferSize;
	private final int waitForNextMsg = 1000;
	private int eventListenerPort = 9090;
	private int clientListenerPort = 9099;

	public ServerConfigImpl() {
		int maxEventSourceBatchSize = getIntEnvVariable("maxEventSourceBatchSize", 100);
		this.messageBufferSize = calculateBufferSize(maxEventSourceBatchSize);
		this.eventListenerPort = getIntEnvVariable("eventListenerPort", eventListenerPort);
		this.clientListenerPort = getIntEnvVariable("clientListenerPort", clientListenerPort);
		this.hostName = getStringEnvVariable("hostName", hostName);
	}

	private String getStringEnvVariable(String key, String defaultValue) {
		String variable = System.getenv(key);
		return variable != null ? variable : defaultValue;
	}

	private int getIntEnvVariable(String key, int defaultValue) {
		String variable = System.getenv(key);
		if (variable != null) {
			try {
				return Integer.valueOf(variable);
			} catch (NumberFormatException e) {
				logger.warn("Environment={} variable is not integer. Will be use "
					+ "defaultValue={}", key, defaultValue);
				return defaultValue;
			}
		} else {
			return defaultValue;
		}
	}

	@Override
	public String getHostName() {
		return hostName;
	}

	@Override
	public int getAccumulateSeconds() {
		return waitForNextMsg;
	}

	@Override
	public int getEventListenerPort() {
		return eventListenerPort;
	}

	@Override
	public int getClientListenerPort() {
		return clientListenerPort;
	}

	public int getMessageBufferSize() {
		return messageBufferSize;
	}

	private int calculateBufferSize(int maxEventSourceBatchSize) {
		if (maxEventSourceBatchSize == 1) {
			return 1;
		} else {
			return maxEventSourceBatchSize / 2;
		}
	}

	@Override
	public String toString() {
		return "ServerConfigImpl{" +
			"hostName='" + hostName + '\'' +
			", messageBufferSize=" + messageBufferSize +
			", waitForNextMsg=" + waitForNextMsg +
			", eventListenerPort=" + eventListenerPort +
			", clientListenerPort=" + clientListenerPort +
			'}';
	}
}
