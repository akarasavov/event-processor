package soundcloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author akt.
 */
public class ServerConfigImpl implements ServerConfig {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private String eventSeparator = "\n";
	private String hostName = "127.0.0.1";
	private int totalEvents = 10000000;
	private int maxEventSourceBatchSize = 100;
	private int eventListenerPort = 9090;
	private int clientListenerPort = 9099;

	public ServerConfigImpl() {
		this.eventSeparator = getStringEnvVariable("eventSeparator", eventSeparator);
		this.totalEvents = getIntEnvVariable("totalEvents", totalEvents);
		this.maxEventSourceBatchSize = getIntEnvVariable("maxEventSourceBatchSize",
			maxEventSourceBatchSize);
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
	public String getEventSeparator() {
		return eventSeparator;
	}

	@Override
	public String getHostName() {
		return hostName;
	}

	@Override
	public int getTotalEvents() {
		return totalEvents;
	}

	@Override
	public int getMaxEventSourceBatchSize() {
		return maxEventSourceBatchSize;
	}

	@Override
	public int getEventListenerPort() {
		return eventListenerPort;
	}

	@Override
	public int getClientListenerPort() {
		return clientListenerPort;
	}


	@Override
	public String toString() {
		return "ServerConfigImpl{" +
			"eventSeparator='" + eventSeparator + '\'' +
			", hostName='" + hostName + '\'' +
			", totalEvents=" + totalEvents +
			", maxEventSourceBatchSize=" + maxEventSourceBatchSize +
			", eventListenerPort=" + eventListenerPort +
			", clientListenerPort=" + clientListenerPort +
			'}';
	}
}
