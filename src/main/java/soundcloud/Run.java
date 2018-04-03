package soundcloud;

import java.io.IOException;
import soundcloud.event.executor.EventExecutorDelegate;
import soundcloud.parser.ClientEventParserImpl;
import soundcloud.parser.SourceEventParserImpl;
import soundcloud.server.EchoWorker;
import soundcloud.server.NioServer;
import soundcloud.user.UserCache;
import soundcloud.user.UserCacheImpl;

/**
 * @author akt.
 */
public class Run {

	private static final String hostName = "127.0.0.1";
	private static final int eventSourcePort = 9090;
	private static final int clientEventPort = 9099;

	public static void main(String[] args) throws IOException {
		UserCache userCache = new UserCacheImpl();
		EventExecutorDelegate eventExecutorDelegate = new EventExecutorDelegate();

		EchoWorker echoWorker = new EchoWorker(new SourceEventParserImpl(), new ClientEventParserImpl(),
			eventExecutorDelegate, userCache);
		new Thread(echoWorker).start();
		NioServer eventServer = new NioServer(hostName, eventSourcePort, 0, echoWorker);
		new Thread(eventServer).start();

		NioServer clientServer = new NioServer(hostName, clientEventPort, 1, echoWorker);
		new Thread(clientServer).start();

		eventExecutorDelegate.build(clientServer, userCache);
	}
}
