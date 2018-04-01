package soundcloud.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import soundcloud.server.event.ChangeEvent;
import soundcloud.server.event.NewMessageEvent;
import soundcloud.util.SocketChannelUtil;

/**
 * @author akt.
 */
public class ServerSocketImpl implements ServerSocket {

	private final static Logger logger = LoggerFactory.getLogger(ServerSocketImpl.class);
	//List of internal events
	private final List<ChangeEvent> changeEvents = new LinkedList<>();
	//Data that should be sendMessage
	private final Map<SocketChannel, ByteBuffer> pendingData = new HashMap<>();

	private final BehaviorSubject<NewMessageEvent> messageObservable = BehaviorSubject.create();
	private final BehaviorSubject<Boolean> isStartedObservable = BehaviorSubject.create();
	private Selector selector;

	public ServerSocketImpl() {
		isStartedObservable.onNext(false);
	}

	@Override
	public void start(String hostName, int port) throws IOException {
		InetSocketAddress address = new InetSocketAddress(hostName, port);
		this.selector = initSelector(address);
		logger.info("Server started on address={}", address);
		isStartedObservable.onNext(true);
		loop(selector);
	}

	@Override
	public void sendMessage(String message, SocketChannel socketChannel) {
		ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
		synchronized (changeEvents) {
			changeEvents.add(new ChangeEvent(socketChannel, ChangeEvent.CHANGEOPS, SelectionKey.OP_WRITE));

			synchronized (pendingData) {
				pendingData.put(socketChannel, buffer);
			}
		}
		this.selector.wakeup();
	}

	@Override
	public Observable<NewMessageEvent> messageObservable() {
		return messageObservable;
	}

	@Override
	public Observable<Boolean> isStartedObservable() {
		return isStartedObservable;
	}

	private Selector initSelector(InetSocketAddress hostAddress) throws IOException {
		Selector selector = Selector.open();
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		serverChannel.socket().bind(hostAddress);
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);

		return selector;
	}

	private void loop(Selector selector) throws IOException {
		logger.debug("Loop function is started");
		while (true) {
			synchronized (changeEvents) {
				changeEvents.forEach(changeEvent -> {
					switch (changeEvent.getEventType()) {
						case ChangeEvent.CHANGEOPS: {
							SelectionKey selectionKey = changeEvent.getSocketChannel().keyFor(selector);
							if (selectionKey != null && selectionKey.isValid()) {
								selectionKey.interestOps(changeEvent.getOperationType());
							} else {
								logger.warn("Selection key is in unvalid state");
							}
							break;
						}
					}
				});
				changeEvents.clear();
			}

			selector.select();
			Iterator keys = selector.selectedKeys().iterator();
			while (keys.hasNext()) {
				SelectionKey key = (SelectionKey) keys.next();
				keys.remove();

				if (!key.isValid()) {
					continue;
				}

				if (key.isAcceptable()) {
					registerClient(key, selector);
				} else if (key.isReadable()) {
					readMessage(key);
				} else if (key.isWritable()) {
					writeMessage(key);
				}
			}
		}
	}

	private void registerClient(SelectionKey key, Selector selector) {
		try {
			ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
			SocketChannel channel = serverChannel.accept();
			channel.configureBlocking(false);
			Socket socket = channel.socket();

			SocketAddress remoteAddress = socket.getRemoteSocketAddress();
			logger.info("Server accept connection from a new client with address={}", remoteAddress);

			channel.register(selector, SelectionKey.OP_READ);
		} catch (IOException e) {
			logger.error("Error while registering client", e);
		}
	}

	private void writeMessage(SelectionKey key) {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		synchronized (pendingData) {
			ByteBuffer byteBuffer = pendingData.remove(socketChannel);
			if (byteBuffer != null) {
				try {
					logger.info("Server try to send message to address={}", socketChannel.getRemoteAddress());
					int length;
					do {
						length = socketChannel.write(byteBuffer);
					} while (length != -1 && byteBuffer.hasRemaining());
					logger.info("Sever socket send message to address={}", socketChannel.getRemoteAddress());
				} catch (IOException e) {
					logger.error("Error in sending message", e);
				}

				if (byteBuffer.remaining() > 0) {
					removeSocketChannel(socketChannel);
				}
			} else {
				logger.warn("Byte Buffer is empty for client=" + socketChannel);
			}
		}
		key.interestOps(SelectionKey.OP_READ);
	}

	private void readMessage(SelectionKey key) {
		SocketChannel channel = (SocketChannel) key.channel();

		try {
			String message = SocketChannelUtil.readMessage(channel);
			logger.debug("Read message={}", message);

			messageObservable.onNext(new NewMessageEvent(channel, message));
		} catch (IOException e) {
			logger.error("Error while reading a message", e);
			removeSocketChannel(channel);
		}
	}

	private void removeSocketChannel(SocketChannel channel) {
		Socket socket = channel.socket();
		SocketAddress remoteAddress = socket.getRemoteSocketAddress();

		logger.info("Connection closed by client={}", remoteAddress);
		try {
			channel.close();
		} catch (IOException e) {
			logger.error("exception when try to close channel", e);
		}
	}
}