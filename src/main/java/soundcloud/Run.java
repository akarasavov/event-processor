package soundcloud;

import java.io.IOException;
import soundcloud.event.executor.EventExecutorDelegate;
import soundcloud.parser.ClientEventParserImpl;
import soundcloud.parser.SourceEventParserImpl;
import soundcloud.server.MessageListener;
import soundcloud.server.ServerSocket;
import soundcloud.server.ServerSocketImpl;
import soundcloud.user.UserCache;
import soundcloud.user.UserCacheImpl;

/**
 * @author akt.
 */
public class Run {

	private static final String hostName = "127.0.0.1";
	private static final int eventSourcePort = 9090;
	private static final int clientEventPort = 9099;

	public static void main(String[] args) {
		ServerSocket eventSourceSocket = new ServerSocketImpl("EventSourceSocket");
		ApplicationExecutors.EVENT_SOURCE_NIO.submit(() -> {
			try {
				eventSourceSocket.start(hostName, eventSourcePort);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		ServerSocket clientEventSocket = new ServerSocketImpl("ClientSocket");
		ApplicationExecutors.CLIENT_NIO.submit(() -> {
			try {
				clientEventSocket.start(hostName, clientEventPort);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		UserCache userCache = new UserCacheImpl();
		MessageListener messageListener = new MessageListener(eventSourceSocket.messageObservable(),
			clientEventSocket.messageObservable(),
			new SourceEventParserImpl(), new ClientEventParserImpl(),
			new EventExecutorDelegate(clientEventSocket, userCache), userCache);
		messageListener.startListen();
	}
}
