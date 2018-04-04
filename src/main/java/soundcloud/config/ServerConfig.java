package soundcloud.config;

/**
 * @author akt.
 */
public interface ServerConfig {

	String EVENT_SEPARATOR = "\n";

	String getEventSeparator();

	String getHostName();

	int getTotalEvents();

	int getMaxEventSourceBatchSize();

	int getEventListenerPort();

	int getClientListenerPort();


}
