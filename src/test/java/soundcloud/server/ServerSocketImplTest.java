package soundcloud.server;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rx.schedulers.Schedulers;
import soundcloud.ApplicationExecutors;
import soundcloud.client.ClientSocket;
import soundcloud.client.ClientSocketImpl;
import soundcloud.server.event.NewMessageEvent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        this.server = new ServerSocketImpl("serverSocket");
        this.client = new ClientSocketImpl();
        this.hostName = InetAddress.getLocalHost().getHostName();

        ExecutorService serveNIOExecutor = ApplicationExecutors.EVENT_SOURCE_NIO;
        serveNIOExecutor.submit(() -> {
            try {
                server.start(hostName, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        ExecutorService clientNIOExecutor = ApplicationExecutors.CLIENT_NIO;
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
        List<String> list = new ArrayList<>();
        server.messageObservable()
                .filter(serverSocketEvent -> serverSocketEvent instanceof NewMessageEvent)
                .map(serverSocketEvent -> (NewMessageEvent) serverSocketEvent)
                .subscribe(newMessageEvent -> {
                    System.out.println("Receive new message=" + newMessageEvent);
                    Assert.assertTrue(newMessageEvent.getMessage().equals(message));
                });
        Thread.sleep(1000);
    }

    public List<String> generateNumbers(int amount) {
        List<String> messages = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            messages.add(i + "");
        }
        return messages;
    }
}