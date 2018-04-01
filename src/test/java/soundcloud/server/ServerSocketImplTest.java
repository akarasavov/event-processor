package soundcloud.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rx.schedulers.Schedulers;
import soundcloud.client.ClientSocket;
import soundcloud.client.ClientSocketImpl;
import soundcloud.server.event.NewMessageEvent;

/**
 * @author akt.
 */
public class ServerSocketImplTest {

	private ServerSocket server;
	private ClientSocket client;
	private String hostName;
	private int port = 9090;

	@Before
	public void setUp() throws Exception {
		this.server = new ServerSocketImpl();
		this.client = new ClientSocketImpl();
		this.hostName = InetAddress.getLocalHost().getHostName();

		ExecutorService serveNIOExecutor = Executors.newSingleThreadExecutor();
		serveNIOExecutor.submit(() -> {
			try {
				server.start(hostName, port);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		ExecutorService clientNIOExecutor = Executors.newSingleThreadExecutor();
		server.isStartedObservable()
			.observeOn(Schedulers.from(clientNIOExecutor))
			.subscribe(started -> {
				if (started) {
					try {
						client.start(new InetSocketAddress(hostName, port));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
	}

	@Test
	public void readMessageOnServer() throws InterruptedException {
		String message = "Test";
		client.isStartedObservable()
			.observeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
			.subscribe(started -> {
				if (started) {
					client.send(message);
				}
			});
		server.messageObservable()
			.filter(serverSocketEvent -> serverSocketEvent instanceof NewMessageEvent)
			.map(serverSocketEvent -> (NewMessageEvent) serverSocketEvent)
			.subscribe(newMessageEvent -> {
				System.out.println("Receive new message=" + newMessageEvent);
				Assert.assertTrue(newMessageEvent.getMessage().equals(message));
			});
		Thread.sleep(1000);
	}
}