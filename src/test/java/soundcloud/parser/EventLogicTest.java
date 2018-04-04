package soundcloud.parser;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import soundcloud.Application;
import soundcloud.SocketClient;
import soundcloud.config.ServerConfigImpl;


public class EventLogicTest {

	private String host;
	private int clientPort;
	private int eventSourcePort;
	private Application application;

	@Before
	public void setUp() throws Exception {
		ServerConfigImpl serverConfig = new ServerConfigImpl();
		this.host = serverConfig.getHostName();
		this.clientPort = serverConfig.getClientListenerPort();
		this.eventSourcePort = serverConfig.getEventListenerPort();
		this.application = new Application(serverConfig);
		application.start();
	}

	@After
	public void tearDown() throws Exception {
		List<Callable<Boolean>> shutdown = application.shutdown();
		shutdown.forEach(elem -> {
			try {
				elem.call();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Test
	public void followOperationTest() throws IOException, InterruptedException, ExecutionException {
		SocketClient firstClient = new SocketClient(host, clientPort);
		firstClient.writeMessage(Collections.singletonList("60\n"));
		firstClient.close();

		SocketClient secondClient = new SocketClient(host, clientPort);
		secondClient.writeMessage(Collections.singletonList("50\n"));

		ScheduledFuture<String> schedule = Executors.newSingleThreadScheduledExecutor()
			.schedule(secondClient::readMessage, 1, TimeUnit.SECONDS);

		SocketClient eventSocket = new SocketClient(host, eventSourcePort);
		String message = "666|F|60|50\n";
		eventSocket.writeMessage(Collections.singletonList(message));

		Assert.assertEquals(message, schedule.get());
	}

	@Test(expected = TimeoutException.class)
	public void unfollowOperationTest() throws IOException, InterruptedException, ExecutionException, TimeoutException {

		SocketClient firstClient = new SocketClient(host, clientPort);
		firstClient.writeMessage(Collections.singletonList("60\n"));
		firstClient.close();

		SocketClient secondClient = new SocketClient(host, clientPort);
		secondClient.writeMessage(Collections.singletonList("50\n"));

		ScheduledFuture<String> schedule = Executors.newSingleThreadScheduledExecutor()
			.schedule(secondClient::readMessage, 1, TimeUnit.SECONDS);

		SocketClient eventSocket = new SocketClient(host, eventSourcePort);
		String message = "666|U|60|50\n";
		eventSocket.writeMessage(Collections.singletonList(message));

		schedule.get(3, TimeUnit.SECONDS);
	}

	@Test
	public void broadcastOperation() throws IOException, InterruptedException, ExecutionException {

		SocketClient firstClient = new SocketClient(host, clientPort);
		firstClient.writeMessage(Collections.singletonList("60\n"));
		ScheduledFuture<String> firstClientRead = Executors.newSingleThreadScheduledExecutor()
			.schedule(firstClient::readMessage, 1, TimeUnit.SECONDS);

		SocketClient secondClient = new SocketClient(host, clientPort);
		secondClient.writeMessage(Collections.singletonList("50\n"));
		ScheduledFuture<String> secondClientRead = Executors.newSingleThreadScheduledExecutor()
			.schedule(secondClient::readMessage, 1, TimeUnit.SECONDS);

		SocketClient eventSocket = new SocketClient(host, eventSourcePort);
		String message = "542532|B\n";
		eventSocket.writeMessage(Collections.singletonList(message));

		Assert.assertEquals(firstClientRead.get(), secondClientRead.get());
		Assert.assertEquals(firstClientRead.get(), message);
	}

	@Test
	public void privateMsgTest() throws IOException, InterruptedException, ExecutionException {

		SocketClient firstClient = new SocketClient(host, clientPort);
		firstClient.writeMessage(Collections.singletonList("60\n"));

		SocketClient secondClient = new SocketClient(host, clientPort);
		secondClient.writeMessage(Collections.singletonList("50\n"));
		ScheduledFuture<String> secondClientRead = Executors.newSingleThreadScheduledExecutor()
			.schedule(secondClient::readMessage, 1, TimeUnit.SECONDS);

		SocketClient eventSocket = new SocketClient(host, eventSourcePort);
		String message = "43|P|60|50\n";
		eventSocket.writeMessage(Collections.singletonList(message));

		Assert.assertEquals(secondClientRead.get(), message);
	}

	@Test
	public void statusUpdate() throws IOException, InterruptedException, ExecutionException {

		SocketClient firstClient = new SocketClient(host, clientPort);
		firstClient.writeMessage(Collections.singletonList("60\n"));

		SocketClient secondClient = new SocketClient(host, clientPort);
		secondClient.writeMessage(Collections.singletonList("50\n"));

		ScheduledFuture<String> firstClientRead = Executors.newSingleThreadScheduledExecutor()
			.schedule(firstClient::readMessage, 1, TimeUnit.SECONDS);

		SocketClient eventSocket = new SocketClient(host, eventSourcePort);
		String message = "675|S|50\n";
		eventSocket.writeMessage(Arrays.asList("666|F|60|50\n", message));
		System.out.println(firstClientRead.get());
		Assert.assertEquals(message, firstClientRead.get());
	}

	@Test(expected = TimeoutException.class)
	public void statusUpdateFail() throws IOException, InterruptedException, ExecutionException, TimeoutException {

		SocketClient firstClient = new SocketClient(host, clientPort);
		firstClient.writeMessage(Collections.singletonList("60\n"));
		ScheduledFuture<String> firstClientRead = Executors.newSingleThreadScheduledExecutor()
			.schedule(firstClient::readMessage, 1, TimeUnit.SECONDS);

		SocketClient eventSocket = new SocketClient(host, eventSourcePort);
		String message = "675|S|50\n";
		eventSocket.writeMessage(Arrays.asList("666|F|60|50\n", message));

		firstClientRead.get(3, TimeUnit.MILLISECONDS);
	}
}
