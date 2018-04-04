package soundcloud.config;


public interface ServerConfig {

	String EVENT_SEPARATOR = "\n";

	String getHostName();

	int getAccumulateSeconds();

	int getEventListenerPort();

	int getClientListenerPort();

	int getMessageBufferSize();
}
