package soundcloud.client;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author akt.
 */
public class FileReaderClient {

	public static void main(String[] args) throws IOException {
		Path path = Paths.get("/home/akt/workspace/workspaceJava/event-processor/test.txt");
		List<String> list = Files.readAllLines(path);
		NioClient client = new NioClient(InetAddress.getLocalHost(), 9090);
		Thread t = new Thread(client);
		t.setDaemon(true);
		t.start();
		RspHandler handler = new RspHandler();
		list.forEach(s -> {
			try {
				System.out.println("send=" + s);
				client.send(s.getBytes(), handler);
				Thread.sleep(100);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		});
	}

}
