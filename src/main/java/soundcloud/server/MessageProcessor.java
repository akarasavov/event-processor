package soundcloud.server;

import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soundcloud.event.entity.EventEntity;
import soundcloud.event.executor.EventExecutor;
import soundcloud.parser.Parser;
import soundcloud.server.event.NewClientEvent;
import soundcloud.server.event.NewMessageEvent;
import soundcloud.server.event.ServerSocketEvent;
import soundcloud.user.UserCache;
import soundcloud.user.UserEntity;

public class MessageProcessor implements Runnable {

	private final List<ServerSocketEvent> queue = new LinkedList<>();
	private final static String CRLF = "\\n";
	private final Parser<EventEntity> sourceEventParser;
	private final Parser<String> clientEventParser;
	private final EventExecutor eventExecutor;
	private final UserCache userCache;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public MessageProcessor(Parser<EventEntity> sourceEventParser, Parser<String> clientEventParser,
		EventExecutor eventExecutor, UserCache userCache) {
		this.sourceEventParser = sourceEventParser;
		this.eventExecutor = eventExecutor;
		this.clientEventParser = clientEventParser;
		this.userCache = userCache;
	}

	void newClient(ServerSocket server, SocketChannel socketChannel) {
		synchronized (queue) {
			queue.add(new NewClientEvent(server, socketChannel));
			queue.notify();
		}
	}

	void newMessage(ServerSocket server, SocketChannel socket, byte[] data, int count) {
		byte[] dataCopy = new byte[count];
		System.arraycopy(data, 0, dataCopy, 0, count);
		synchronized (queue) {
			queue.add(new NewMessageEvent(server, socket, dataCopy));
			queue.notify();
		}
	}

	public void run() {
		while (true) {
			// Wait for data to become available
			ServerSocketEvent serverSocketEvent;
			synchronized (queue) {
				while (queue.isEmpty()) {
					try {
						queue.wait();
					} catch (InterruptedException ignored) {
					}
				}
				serverSocketEvent = queue.remove(0);
			}
			processMessage(serverSocketEvent);
		}
	}

	private void processMessage(ServerSocketEvent serverSocketEvent) {
		if (serverSocketEvent.getServerSocket().getType() == 0 && serverSocketEvent instanceof NewMessageEvent) {
			NewMessageEvent newMessage = (NewMessageEvent) serverSocketEvent;
			String message = new String(newMessage.getData(), StandardCharsets.UTF_8);
			Arrays.stream(message.split(CRLF))
				.map(sourceEventParser::parse)
				.filter(Objects::nonNull)
				.forEach(eventExecutor::execute);
		} else if (serverSocketEvent.getServerSocket().getType() == 1
			&& serverSocketEvent instanceof NewMessageEvent) {
			NewMessageEvent newMessageEvent = (NewMessageEvent) serverSocketEvent;
			String message = new String(newMessageEvent.getData(), StandardCharsets.UTF_8);
			String userCode = clientEventParser.parse(message);
			userCache.addUser(userCode, new UserEntity(userCode, newMessageEvent.getSocketChannel()));
		} else {
			logger.info("MessageProcessor receive unsupported message={}", serverSocketEvent);
		}
	}
}
