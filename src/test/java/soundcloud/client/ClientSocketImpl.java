package soundcloud.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
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
import soundcloud.util.SocketChannelUtil;

/**
 * This class is introduced only for test reason
 *
 * @author atk
 */
public class ClientSocketImpl implements ClientSocket {

	private Selector selector;
	private final List<ChangeEvent> changeEvents = new LinkedList<>();
	private final Map<SocketChannel, ByteBuffer> pendingData = new HashMap<>();
	private SocketChannel coreSocketChannel;
	private final BehaviorSubject<byte[]> messageBehaviorSubject = BehaviorSubject.create();
	private final BehaviorSubject<Boolean> isStartedObservable = BehaviorSubject.create();

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public ClientSocketImpl() {
		isStartedObservable.onNext(false);
	}

	@Override
	public void start(InetSocketAddress hostAddress) throws IOException {
		this.selector = SelectorProvider.provider().openSelector();
		this.coreSocketChannel = initiateConnection(hostAddress);
		logger.debug("run client socket in address={}", hostAddress);
		isStartedObservable.onNext(true);
		loop();
	}

	@Override
	public void send(String message) {
		logger.debug("Client worker send signal for new message={}", message);

		ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());

		synchronized (changeEvents) {
			changeEvents.add(new ChangeEvent(coreSocketChannel, ChangeEvent.CHANGEOPS, SelectionKey.OP_WRITE));

			synchronized (pendingData) {
				pendingData.putIfAbsent(coreSocketChannel, buffer);
			}
		}

		selector.wakeup();
	}

	@Override
	public Observable<byte[]> messageObservable() {
		return messageBehaviorSubject;
	}

	@Override
	public Observable<Boolean> isStartedObservable() {
		return isStartedObservable;
	}

	private void loop() throws IOException {
		while (true) {
			synchronized (changeEvents) {
				for (ChangeEvent changeEvent : changeEvents) {
					if (changeEvent.getEventType() == ChangeEvent.REGISTER) {
						try {
							changeEvent.getSocketChannel().register(selector, changeEvent.getOperationType());
						} catch (ClosedChannelException e) {
							logger.error("Close channel exception", e);
						}
					} else if (changeEvent.getEventType() == ChangeEvent.CHANGEOPS) {
						SelectionKey selectionKey = changeEvent.getSocketChannel().keyFor(selector);
						selectionKey.interestOps(changeEvent.getOperationType());
					}
				}
				changeEvents.clear();
			}

			selector.select();

			Iterator selectedKeys = selector.selectedKeys().iterator();
			while (selectedKeys.hasNext()) {
				SelectionKey key = (SelectionKey) selectedKeys.next();
				selectedKeys.remove();

				if (!key.isValid()) {
					continue;
				}

				if (key.isConnectable()) {
					finishConnection(key);
				} else if (key.isReadable()) {
					readMessage(key);
				} else if (key.isWritable()) {
					writeMessage(key);
				}
			}
		}
	}

	//readMessage from the socket channel
	private void readMessage(SelectionKey key) {
		SocketChannel channel = (SocketChannel) key.channel();

		try {
			String message = SocketChannelUtil.readMessage(channel);
			logger.debug("Read message={}", message);

			messageBehaviorSubject.onNext(message.getBytes());
		} catch (IOException e) {
			logger.error("Error while reading a message", e);
			removeSocketChannel(key);
		}
	}

	private void writeMessage(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		synchronized (pendingData) {
			ByteBuffer byteBuffer = pendingData.remove(socketChannel);
			if (byteBuffer != null) {
				logger.info("Nio thread send message");
				socketChannel.write(byteBuffer);
				if (byteBuffer.remaining() > 0) {
					removeSocketChannel(key);
				}

			}
		}
		key.interestOps(SelectionKey.OP_READ);
	}

	private void removeSocketChannel(SelectionKey key) {
		SocketChannel channel = (SocketChannel) key.channel();
		Socket socket = channel.socket();
		SocketAddress remoteAddress = socket.getRemoteSocketAddress();
		logger.info("Connection closed by client={}", remoteAddress);

		try {
			channel.close();
			key.cancel();
		} catch (IOException e) {
			logger.error("exception when try to close channel", e);
		}
	}

	private SocketChannel initiateConnection(InetSocketAddress hostAddress) throws IOException {
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
		socketChannel.connect(hostAddress);

		synchronized (changeEvents) {
			changeEvents.add(new ChangeEvent(socketChannel, ChangeEvent.REGISTER, SelectionKey.OP_CONNECT));
		}

		return socketChannel;
	}

	private void finishConnection(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		try {
			socketChannel.finishConnect();
			logger.info("Finish connection address={}", socketChannel.getRemoteAddress());
		} catch (IOException e) {
			// Cancel the channel's registration with our selector
			logger.error("Exception when try to finish connection ", e);
			key.cancel();
			return;
		}
		key.interestOps(SelectionKey.OP_WRITE);
	}

}
