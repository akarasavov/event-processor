package soundcloud.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soundcloud.server.event.NewClientEvent;
import soundcloud.server.event.NewMessageEvent;

/**
 * @author akt.
 */
public class NioServer implements Runnable, ServerSocket {

	private Logger logger = LoggerFactory.getLogger(NioServer.class);
	private final EventProcessor eventProcessor;
	private final int type;
	private Selector selector;
	private ByteBuffer readBuffer = ByteBuffer.allocate(8196);
	private final List<ChangeRequest> pendingChanges = new LinkedList<>();
	private final Map<SocketChannel, List<ByteBuffer>> pendingData = new HashMap<>();

	public NioServer(String hostAddress, int port, int type, EventProcessor eventProcessor) throws IOException {
		this.selector = initSelector(hostAddress, port);
		this.eventProcessor = eventProcessor;
		this.type = type;
	}

	public void send(SocketChannel socket, byte[] data) {
		synchronized (this.pendingChanges) {
			this.pendingChanges.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

			synchronized (this.pendingData) {
				List<ByteBuffer> queue = this.pendingData.get(socket);
				if (queue == null) {
					queue = new ArrayList<>();
					this.pendingData.put(socket, queue);
				}
				queue.add(ByteBuffer.wrap(data));
			}
		}
		this.selector.wakeup();
	}

	@Override
	public int getType() {
		return type;
	}

	public void run() {
		try {
			loop();
		} catch (IOException e) {
			logger.error("Error in loop function", e);
		}
	}

	private void loop() throws IOException {
		while (true) {
			synchronized (this.pendingChanges) {
				for (ChangeRequest change : this.pendingChanges) {
					switch (change.type) {
						case ChangeRequest.CHANGEOPS:
							SelectionKey key = change.socket.keyFor(this.selector);
							if (key != null && key.isValid()) {
								key.interestOps(change.ops);
							} else {
								logger.warn("SelectionKey for socket={} is null", change.socket);
							}
					}
				}
				this.pendingChanges.clear();
			}

			this.selector.select();

			Iterator selectedKeys = this.selector.selectedKeys().iterator();
			while (selectedKeys.hasNext()) {
				SelectionKey key = (SelectionKey) selectedKeys.next();
				selectedKeys.remove();

				if (!key.isValid()) {
					continue;
				}

				if (key.isAcceptable()) {
					this.accept(key);
				} else if (key.isReadable()) {
					this.read(key);
				} else if (key.isWritable()) {
					this.write(key);
				}
			}

		}
	}

	private void accept(SelectionKey key) throws IOException {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);

		socketChannel.register(this.selector, SelectionKey.OP_READ);
		eventProcessor.newEvent(new NewClientEvent(this, socketChannel));
	}

	private void read(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		this.readBuffer.clear();

		try {
			int readBytes = socketChannel.read(this.readBuffer);
			if (readBytes == -1) {
				key.channel().close();
				key.cancel();
			} else {
				byte[] dataCopy = new byte[readBytes];
				System.arraycopy(readBuffer.array(), 0, dataCopy, 0, readBytes);
				eventProcessor.newEvent(new NewMessageEvent(this, socketChannel, dataCopy));
			}
		} catch (IOException e) {
			key.cancel();
			socketChannel.close();
		}
	}

	private void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		logger.info("Send message to client={}", socketChannel);

		synchronized (this.pendingData) {
			List queue = (List) this.pendingData.get(socketChannel);

			while (!queue.isEmpty()) {
				ByteBuffer buffer = (ByteBuffer) queue.get(0);
				socketChannel.write(buffer);
				if (buffer.remaining() > 0) {
					break;
				}
				queue.remove(0);
			}

			if (queue.isEmpty()) {
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}

	private Selector initSelector(String host, int port) throws IOException {
		Selector socketSelector = SelectorProvider.provider().openSelector();

		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		InetSocketAddress isa = new InetSocketAddress(host, port);
		serverChannel.socket().bind(isa);
		serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

		return socketSelector;
	}

}

