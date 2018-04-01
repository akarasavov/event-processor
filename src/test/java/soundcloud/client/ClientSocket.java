package soundcloud.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import rx.Observable;

/**
 * @author akt
 */
public interface ClientSocket {

	void start(InetSocketAddress hostAddress) throws IOException;

	void send(String message);

	Observable<byte[]> messageObservable();

	Observable<Boolean> isStartedObservable();
}
