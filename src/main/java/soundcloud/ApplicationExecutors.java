package soundcloud;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author akt.
 */
public class ApplicationExecutors {

	private ApplicationExecutors() {
	}

	//Nio Executors are responsible for performing network operation
	public final static ExecutorService EVENT_SOURCE_NIO = createExecutorService();
	public final static ExecutorService CLIENT_NIO = createExecutorService();

	public final static ExecutorService CLIENT_WORKER = createExecutorService();

	private static ExecutorService createExecutorService() {
		return Executors.newSingleThreadExecutor();
	}

}
