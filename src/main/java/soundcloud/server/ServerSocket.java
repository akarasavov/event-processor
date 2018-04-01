package soundcloud.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import rx.Observable;
import soundcloud.server.event.NewMessageEvent;

/**
 * @author akt.
 */
public interface ServerSocket {

	void start(String hostName, int port) throws IOException;

	void sendMessage(String message, SocketChannel socketChannel);

	Observable<NewMessageEvent> messageObservable();

	Observable<Boolean> isStartedObservable();
}
